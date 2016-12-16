describe('NamesCtrl', function() {
  var $httpBackend;
  var $componentController;
  var ctrl;

  beforeEach(module(me.horlick.hello.names.Module.name));

  /** @ngInject */
  function setUp($injector) {
    // Set up the mock http service responses.
    $httpBackend = $injector.get('$httpBackend');
    // backend definition common for all tests.
    $httpBackend.when('GET', '/v1/greeter/names').respond({
      'entry': [
        {'name': 'Leonard', 'count': '1'}, {'name': 'Penny', 'count': '2'},
        {'name': 'Rajesh', 'count': '1'}, {'name': 'Sheldon', 'count': '4'}
      ]
    });

    $componentController = $injector.get('$componentController');
  }
  beforeEach(inject(setUp));

  it('should request names from backend', function() {
    // Instantiate the controller.
    ctrl = $componentController('names');

    $httpBackend.expect('GET', '/v1/greeter/names');
    expect($httpBackend.flush).not.toThrow();
  });

});
