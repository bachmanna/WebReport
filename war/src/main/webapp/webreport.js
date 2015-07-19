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
	
	app.controller('WebReportController',  ['$http', 'queryFilter', function($http, queryFilter){
		this.studies = [];
		this.reporting = false;
		this.reportingStudy = undefined;
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
				console.log('success: data = ' + JSON.stringify(data));
				ctrl.reportingStudy = study;
				ctrl.reporting = true;
				
				if(data){
					console.log('setting editor data = ' + data.report);
					ctrl.ckEditor.setData(data.report);
				} else {
					console.log('setting editor data = <p></p>');
					ctrl.ckEditor.setData('<p></p>');
				}
				
			}).error(function(data){
				console.log('error: data = ' + JSON.stringify(data));
//	TODO do something
			});
			
		};
		
		this.cancelReport = function(){
			ctrl.reporting = false;
			ctrl.reportingStudy = undefined;
		};
		
		this.submitReport = function(filterCtrl){
			$http.post('/webreport/report/byStudy/' + ctrl.reportingStudy.pk, 
						ctrl.ckEditor.getData(),
						{headers : {'Content-Type' : 'text/html'}}
			).success(function(data){
				ctrl.query();
				ctrl.reporting = false;
				ctrl.reportingStudy = undefined;
				
			});
		};

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
		            })
		          })
		       }
		    }
		})
})();