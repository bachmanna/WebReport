(function(){
	var app = angular.module('WebReport', ['ui.bootstrap', 'ngIdle']);
	
	app.config(function(KeepaliveProvider, IdleProvider) {
		var idleTimeout = 60							// 5 min
		IdleProvider.idle(idleTimeout);
		IdleProvider.timeout(0);						// no warning period
		KeepaliveProvider.interval(idleTimeout/2);		// some leg room
	});

	// debbuger
	window.onerror = function(msg, url, line){
		console.log('Error: ' + msg);
		console.log('URL: ' + url);
		console.log('Line#: ' + line);
	};

	// provides access to the query parameters to any controller
	app.service('queryFilter', function() {
		var queryFilter = {
				pid : null, 	// patien id
				pna : null,		// patient name
				acc : null,		// accession number
				sd : null,		// start date
				ed : null,		// end date
				sm : null,		// modalities in study
				rs : 0			// report status: 0 = all, 1 = typed, 2 = released, 
		};
	    
	    return {
	        getQueryFilter : function() {
	            return queryFilter;
	        }
	    }
	});

	// maps query parameters to form elements
	app.controller('SearchFilterController',  ['queryFilter', function(queryFilter){
		this.reportStatusValues = ['Todos', 'Digitados', 'Liberados'];
		this.queryParams = queryFilter.getQueryFilter();
	}]);
	
	// 
	app.controller('PopupController',  ['$modalInstance', 'modalMsg', 'showOkBtn', function($modalInstance, modalMsg, showOkBtn){
		this.modalMsg = modalMsg;
		this.showOkBtn = showOkBtn;
		
		this.ok = function () {
			$modalInstance.close();
		};

		this.cancel = function () {
			$modalInstance.dismiss();
		};
	}]);
	
	// main controller
	app.controller('WebReportController',  ['$scope', '$http', '$sce', '$modal', '$log', 'Idle', 'Keepalive', 'queryFilter', function($scope, $http, $sce, $modal, $log, Idle, Keepalive, queryFilter){
		this.studies = [];							// query result
		this.reporting = false;						// query/report state change controller
		this.reportingStudy = undefined;			// selected study being reported
		this.reportingStudyReports = undefined;		// previous reports and amendments
		this.ckEditor = undefined;					// rich text editor
		this.reportingStudyLock = undefined;		// string representing a lock object, used to prevent simultaneous reporting of the same study
		this.userIsIdle = false;
		
		var ctrl = this;

		// renew study lock
		$scope.$on('Keepalive', function() {
			$log.info('Study lock KeepAlive');
			if(ctrl.userIsIdle){
				$log.info('Study lock KeepAlive: user is idle, ignoring keepalive');
			} else {
				$http.post('/webreport/study/' + ctrl.reportingStudy.pk +'/lock/' + reportingStudyLock).success(function(data){
					if(status < 400){
						console.log('Renewed study lock');
					} else {
						//TODO do something! alert the user
						console.log('Error renewing study lock: ');
						console.log(JSON.stringify(data));
					}
				});
			}
		});
		
		// user is idle
		$scope.$on('IdleStart', function() {
			$log.info('user is idle');
			Idle.unwatch();		// stops detection of user inactivity
			ctrl.userIsIdle = true;
			
			ctrl.showWarningPopup('A atividade de laudo expirou devido a inatividade. Deseja continuar o laudo? ', true, '',
				function() {				// ok callback
					$log.info('Trying to resume idlereport');
					ctrl.reportingStudy.locked = false;
					ctrl.report(reportingStudy, true);
				}, 
				function() {				// cancel callback
					$log.info('Idle report canceled');
					ctrl.cancelReport();
				}
			);
		});

		this.showWarningPopup = function(message, showOkBtn, size, okCallback, dissmissCallback){
			var modalInstance = $modal.open({
				animation : false,
				templateUrl : 'ngtemplates/reportIdlePopup.html',
				controller : 'PopupController as ctrl',
				size : size,
				resolve: {
					modalMsg : function () {
						return message;
					},
					showOkBtn : function () {
						return showOkBtn;
					}
				},
			});

			modalInstance.result.then(okCallback, dissmissCallback);
		};

		// locks the study in the server for reporting
		this.lockStudy = function(study){
			$log.info('locking study for report: ' + study.pk + ' - ' + study.patientName);
			return $http.get('/webreport/study/' + study.pk + '/lock/');
		};
		
		// refresh the study table (used after reporting, or some timeout)
		this.refreshSearch = function(){
			ctrl.query();
			ctrl.reporting = false;
			ctrl.reportingStudy = undefined;
			ctrl.ckEditor.setData('<p></p>');
		};
		
		// Selects the icon for the study table. Might replace this for a generic status icon (representing lock status, for instance)
		this.reportStatusIcon = function(study){
			if(study.reportStatus === 'typed') return '/webreport/styles/reportTyped.png';
			if(study.reportStatus === 'released') return '/webreport/styles/reportReleased.png';
			if(study.reportStatus === 'amended') return '/webreport/styles/reportAmended.png';
			return '';
//			return '/styles/reportNothing.png';
		};
		
		// search for studies using parameters defined on the query form
		this.query = function(){
			var queryParams = queryFilter.getQueryFilter();
			
			$log.info('query: ' + JSON.stringify(queryParams));
			$http.get('/webreport/study', {params : queryParams}).success(function(data){
				ctrl.studies = data.map(function(el){
					 // used for ng-class
					if(el.locked){
						el.locked = 'locked';
					} else {
						delete el.locked;
					};
					return el;
				});
			});
		};
		
		// Starts reporting the selected study
		this.report = function(study, isResumingIdle){
			if(study.locked){
				// TODO show read-only report? what's the use, someone is working on it 
				$log.info('study already locked');
				return;
			}
			
			// lock the study in the server
			ctrl.lockStudy(study).success(function(data){
				$log.info('locked study, lock id = ' + data);
				reportingStudyLock = data;
				
				$log.info('downloading reports of study ' + study.pk + ' - ' + study.patientName);
				$http.get('/webreport/report/byStudy/' + study.pk).success(function(data){
					Idle.watch();		// starts detection of user inactivity					
					
					var releasedReports = [];
					$log.info('study reports = ' + JSON.stringify(data));
					ctrl.reportingStudy = study;
					ctrl.reporting = true;
					
					// init report screen
					var newEditorData = '<p></p>';
					
					var resumedReport = undefined;
					if(isResumingIdle){
						resumedReport = ctrl.reportingStudyReports[ctrl.reportingStudyReport.length-1];
					}
					
					if(data){
						releasedReports = [data];
						if(data.amendments){
							releasedReports.push.apply(releasedReports, data.amendments);
							releasedReports.sort(function(a, b){
								return a.reportDatetime - b.reportDatetime;
							});
						}
						
						var lastReport = releasedReports[releasedReports.length-1];
						if(lastReport.status === 'typed'){
							releasedReports.splice(-1, 1);
							newEditorData = lastReport.report;
						}
						
						ctrl.reportingStudyReports = releasedReports.map(function(el){
							$log.info('sanitising ' + JSON.stringify(el));
							el.releaseDateTime = el.reportDatetime || el.amendmentDatetime 
							el.report = $sce.trustAsHtml(el.report);	// sanitization service removes styles
							return el;
						});
					} else {
						ctrl.reportingStudyReports = [];
					}
					
					if(isResumingIdle){
						if(resumedReport.releaseDateTime != lastReport.releaseDateTime){
							$log.warn('Report timestamp changed: new = ' + lastReport.releaseDateTime + ' current = ' + resumedReport.releaseDateTime);
							ctrl.showWarningPopup('A cadeia de laudos do exame mudou durante o período de inatividade.\n\n Não é possível continuar o laudo. ', false, '', undefined,
								function() {				// cancel callback
									$log.info('Idle report canceled');
									ctrl.cancelReport();
								}
							);
						}
						
						// no need to clear/reset the text editor
						return;
					}	
					
					ctrl.ckEditor.setData(newEditorData);
					
				}).error(function(data){
					$log.info('Get reports error: data = ' + JSON.stringify(data));
//		TODO do something
				});
			}).error(function(data){
				$log.info('Lock study error: data = ' + JSON.stringify(data));
//TODO do something
			});
		};
		
		// abort report typing
		this.cancelReport = function(){
			$log.info('unlocking study' + ctrl.reportingStudy.pk);
			Idle.unwatch();		// stops detection of user inactivity
			// release lock
			$http({url: '/webreport/study/' + ctrl.reportingStudy.pk +'/lock/' + reportingStudyLock, method: 'DELETE'}).success(function(data){
				ctrl.refreshSearch();
			}).error(function(data){
				$log.info('error: data = ' + JSON.stringify(data));
//TODO do something
			});
		};
		
		this.submitReport = function(release){
			var queryParams = {rel : release ? "1" : undefined};
			$http.post('/webreport/report/byStudy/' + ctrl.reportingStudy.pk + '/' + ctrl.reportingStudyLock, 
						ctrl.ckEditor.getData(),
						{
							headers : {'Content-Type' : 'text/html'},
							params : queryParams
						}
			).success(function(data){
				Idle.unwatch();		// stops detection of user inactivity
				ctrl.refreshSearch();
			}).error(function(data){
				$log.info('error: data = ' + JSON.stringify(data));
			});
		};
		
		// inserts rich editor in the document
		this.insertEditor = function(component){
			ctrl.ckEditor = CKEDITOR.replace(component);
		};
		
	}]);
	
	app.directive("searchSection", function() {
		return {
			restrict: 'E',
			templateUrl: 'ngtemplates/search.html',
			scope: {
				reportctrl: '='
			},
		};
	});

	app.directive("reportSection", function() {
		return {
			restrict: 'E',
			templateUrl: 'ngtemplates/report.html',
			scope: {
				reportctrl: '='
			},
		};
	});
	
	app.directive( 'elemReady', function( $parse ) {
		return {
			restrict: 'A',
			link: function( $scope, elem, attrs ) {    
				elem.ready(function(){
					$scope.$apply(function(){
						var func = $parse(attrs.elemReady);
						func($scope);
					});
				});
			}
		};
	});
})();