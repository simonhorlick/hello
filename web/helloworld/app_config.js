goog.require('me.horlick.hello.app')

me.horlick.hello.app.config([
  '$locationProvider', '$routeProvider',
  function config($locationProvider, $routeProvider) {
    $locationProvider.hashPrefix('!');

    $routeProvider.when('/names', {template: '<names></names>'})
        .otherwise('/names');
  }
]);
