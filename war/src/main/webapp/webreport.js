(function(){
	var app = angular.module('WebReport', []);

	window.onerror = funcion(msg, url, line){
		console.log('Error: ' + msg);
		console.log('URL: ' + url);
		console.log('Line#: ' + line);
	};
	
	app.service('queryFilter', function() {
		var queryFilter = {
				pid : null, 
				pna : null,
				acc : null,
				sd : null,
				ed : null,
				sm : null,
				rs : 0
		};
	    
	    return {
	        getQueryFilter : function() {
	            return queryFilter;
	        }
	    }
	});

	app.controller('SearchFilterController',  ['queryFilter', function(queryFilter){
		this.reportStatusValues = ['Todos', 'Digitados', 'Liberados'];
		this.queryParams = queryFilter.getQueryFilter();
	}]);
	
	app.controller('WebReportController',  ['$http', '$sce', 'queryFilter', function($http, $sce, queryFilter){
		this.studies = [];
		this.reporting = false;
		this.reportingStudy = undefined;
		this.reportingStudyReports = undefined;
		this.ckEditor = undefined;
		this.reportingStudyLock = undefined;
		
		var ctrl = this;
		console.log('WebReportController (this): ' + JSON.stringify(this));
		console.log('WebReportController (var): ' + JSON.stringify(ctrl));

		this.lockStudy = function(study){
			console.log('locking study for report: ' + study.pk + ' - ' + study.patientName);
			return $http.get('/webreport/study/' + study.pk + '/lock/');
		};
		
		this.refreshSearch = function(){
			ctrl.query();
			ctrl.reporting = false;
			ctrl.reportingStudy = undefined;
			ctrl.ckEditor.setData('<p></p>');
		};
		
		this.reportStatusIcon = function(study){
			if(study.reportStatus === 'typed') return '/webreport/styles/reportTyped.png';
			if(study.reportStatus === 'released') return '/webreport/styles/reportReleased.png';
			if(study.reportStatus === 'amended') return '/webreport/styles/reportAmended.png';
			return '';
//			return '/styles/reportNothing.png';
		};
		
		this.query = function(){
			var queryParams = queryFilter.getQueryFilter();
			
			console.log('query: ' + JSON.stringify(queryParams));
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
		
		this.report = function(study){
			if(study.locked){
				console.log('study already locked');
				return;
			}
			
			ctrl.lockStudy(study).success(function(data){
				console.log('locked study, lock id = ' + data);
				reportingStudyLock = data;
				
				console.log('downloading reports of study ' + study.pk + ' - ' + study.patientName);
				$http.get('/webreport/report/byStudy/' + study.pk).success(function(data){
					var releasedReports = [];
					console.log('study reports = ' + JSON.stringify(data));
					ctrl.reportingStudy = study;
					ctrl.reporting = true;
					
					ctrl.ckEditor.setData('<p></p>');
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
							ctrl.ckEditor.setData(lastReport.report);
						}
					}
					// damn sanitization removes styles
					ctrl.reportingStudyReports = releasedReports.map(function(el){
						console.log('sanitising ' + JSON.stringify(el));
						el.releaseDateTime = el.reportDatetime || el.amendmentDatetime
						el.report = $sce.trustAsHtml(el.report);
						return el;
					});
					
				}).error(function(data){
					console.log('error: data = ' + JSON.stringify(data));
//		TODO do something
				});
			}).error(function(data){
				console.log('error: data = ' + JSON.stringify(data));
//TODO do something
			});
		};
		
		this.cancelReport = function(){
			console.log('unlocking study' + ctrl.reportingStudy.pk);
			$http({url: '/webreport/study/' + ctrl.reportingStudy.pk +'/lock/' + reportingStudyLock, method: 'DELETE'}).success(function(data){
				ctrl.refreshSearch();
			}).error(function(data){
				console.log('error: data = ' + JSON.stringify(data));
//TODO do something
			});
		};
		
		this.submitReport = function(release, dontRetry){
			var queryParams = {rel : release ? "1" : undefined};
			$http.post('/webreport/report/byStudy/' + ctrl.reportingStudy.pk + '/' + ctrl.reportingStudyLock, 
						ctrl.ckEditor.getData(),
						{
							headers : {'Content-Type' : 'text/html'},
							params : queryParams
						}
			).success(function(data){
				ctrl.refreshSearch();
			}).error(function(data){
				if(dontRetry){
					console.log('error: data = ' + JSON.stringify(data));
				} else {
					//TODO this is an optmistic aproach. we should reload the report data from the server, and only proceed if there were no changes
					console.log('submit fail, retrying lock');
					ctrl.lockStudy(ctrl.reportingStudy).success(function(newdata){
						console.log('re-locked study ' + ctrl.reportingStudy.pk);
						ctrl.reportingStudyLock = newdata;
						ctrl.submitReport(release, true);
					}).error(function(data){
						console.log('error: data = ' + JSON.stringify(data));
		//TODO do something
					});
				}
			});
		};
		

		this.insertEditor = function(component){
			ctrl.ckEditor = CKEDITOR.replace(component);
		};
		
	}]);

	app.controller("ReportPopupController", [function(){
		
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
		            })
		          })
		       }
		    }
		})
})();