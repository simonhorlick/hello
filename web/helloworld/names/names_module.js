goog.provide('me.horlick.hello.names.Module')

goog.require('me.horlick.hello.names.NamesCtrl')

/**
 *  @type {angular.Module}
 */
me.horlick.hello.names.Module =
    angular.module('me.horlick.hello.names.Module', []).component('names', {
      templateUrl: 'names/names_template.html',
      controller: me.horlick.hello.names.NamesCtrl
    });
