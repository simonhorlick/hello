goog.provide('me.horlick.hello.app')
goog.require('me.horlick.hello.names.Module')

me.horlick.hello.app = angular.module('me.horlick.hello.app', [
  'ngRoute',
  'ngMaterial',
  me.horlick.hello.names.Module.name,
]);
