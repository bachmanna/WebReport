(function(){
	var app = angular.module('WebReport', []);
	
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
		
		var ctrl = this;
		console.log('WebReportController (this): ' + JSON.stringify(this));
		console.log('WebReportController (var): ' + JSON.stringify(ctrl));
		
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
				ctrl.studies = data;
			});
		};
		
		this.report = function(study){
			console.log('downloading reports of study ' + study.pk + ' - ' + study.patientName);
			
			$http.get('/webreport/report/byStudy/' + study.pk).success(function(data){
				var releasedReports = [];
				console.log('success: data = ' + JSON.stringify(data));
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
					console.log('el=' + JSON.stringify(el));
					el.releaseDateTime = el.reportDatetime || el.amendmentDatetime
					el.report = $sce.trustAsHtml(el.report);
					return el;
				});
				
			}).error(function(data){
				console.log('error: data = ' + JSON.stringify(data));
//	TODO do something
			});
		};
		
		this.cancelReport = function(){
			ctrl.reporting = false;
			ctrl.reportingStudy = undefined;
			ctrl.ckEditor.setData('<p></p>');
		};
		
		this.submitReport = function(release){
			var queryParams = {rel : release ? "1" : undefined};
			$http.post('/webreport/report/byStudy/' + ctrl.reportingStudy.pk, 
						ctrl.ckEditor.getData(),
						{
							headers : {'Content-Type' : 'text/html'},
							params : queryParams
						}
			).success(function(data){
				ctrl.query();
				ctrl.reporting = false;
				ctrl.reportingStudy = undefined;
				ctrl.ckEditor.setData('<p></p>');
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