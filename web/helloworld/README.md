# helloworld web client

## Dependencies

First install the developer tools that are used by this project:
```shell
$ apt-get install nodejs-legacy npm
$ sudo npm install -g bower
```

Then install the dependencies for this app:
```shell
$ bower install
```

Google Closure is used to ensure that all dependencies are declared in the correct order, if not you'll see an error such as `goog.require could not find: me.horlick.hello.names.NamesCtrl`

## Developing

Run the development server
```shell
$ npm start
```

Now browse to the app at localhost:8000/index.html.
