'use strict';

var gwezModule = angular.module('gwez', [], function ($routeProvider, $locationProvider) {
    $routeProvider.when('/', { templateUrl:'ng/home.html' });
    $routeProvider.otherwise({redirectTo:'/'});
    $locationProvider.html5Mode(true);
});
