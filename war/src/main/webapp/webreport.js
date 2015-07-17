(function(){
	var app = angular.module('WebReport', []);
	
		
	app.controller('SearchFilterController',  function(){
		this.patientName = null;
		this.patientId = null;
		this.accessionNumber = null;
		this.startDate = null;
		this.endDate = null;
		this.modality = null;
	});
	
	app.controller('WebReportController',  ['$http', function($http){
		this.studies = {};
		var ctrl = this;
		reporting = false;
		reportingStudy = null;

		this.query = function(filterCtrl){
			this.studies = $http.get('/webreport/study', { 
															params : { 
																		pid : filterCtrl.patientId, 
																		pna : filterCtrl.patientName,
																		acc : filterCtrl.accessionNumber,
																		sd : filterCtrl.startDate,
																		ed : filterCtrl.endDate,
																		sm : filterCtrl.modality
																	}
														}
			).success(function(data){
				ctrl.studies = data;
			});
		};
		
		this.report = function(study){
			ctrl.reporting = true;
			ctrl.reportingStudy = study;
		};
		
		this.submitReport = function(ckEditor){
			$http.put('/webreport/report/' + reportingStudy.pk, ckEditor.data).success(function(data){
				
			});
		}

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
})();