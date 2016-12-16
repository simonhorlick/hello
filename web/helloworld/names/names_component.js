goog.provide('me.horlick.hello.names.NamesCtrl')

/**
 * Displays the most commonly greeted names.
 *
 * @param {!angular.$http} $http
 *
 * @constructor
 * @ngInject
 * @export
 */
me.horlick.hello.names.NamesCtrl = function NamesCtrl($http) {
  /** @export */
  this.entry = [];

  var self = this;

  $http.get('http://localhost:50052/v1/greeter/names')
      .then(function(response) {
        self.entry = response.data.entry;
        console.log(JSON.stringify(response.data));
      })
      .catch(function() { console.log('Request failed.'); });
};
