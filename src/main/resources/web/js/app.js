'use strict';

var gwezModule = angular.module('battle', [], function ($routeProvider, $locationProvider) {
    $routeProvider.when('/', { templateUrl:'ng/home.html' });
    $routeProvider.otherwise({redirectTo:'/'});
    $locationProvider.html5Mode(true);
});
