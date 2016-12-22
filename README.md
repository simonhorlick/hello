# hello

A hello world application.

## Architecture Overview

hello consists of an [Android](/java/me/horlick/apps/greeter) frontend app and a set of backend services exposing [gRPC](http://www.grpc.io) endpoints.
As gRPC is language agnostic, the backend services may be written in any language.
To glue everything together, each service is packaged as a [Docker](https://www.docker.com) container and scheduled onto machines using [Kubernetes](http://kubernetes.io).

hello is built and tested using [Bazel](https://bazel.io).
Each commit triggers a [Jenkins](https://ci.horlick.me/job/hello/) build which runs all unit, integration and UI tests.

### Components

[greeter](/java/me/horlick/apps/greeter) -- An Android frontend app that allows users to request a greeting from the `helloworld` server, which is then displayed to them.

[helloworld.proto](/protos/helloworld.proto) -- The service definition for the Greeter service.

[helloworld](/java/me/horlick/helloworld) -- A server that implements the Greeter service.

[helloworld_gateway_server](/go) -- A REST-based translation server for `helloworld`.

[hellobigbang](/java/me/horlick/hellobigbang) -- Inserts random test data into the database.

[vitess](/infra/db) -- The database that stores greetings.

## Building

To build all targets simply run:
```shell
$ bazel build ...
```

Similarly, to test all targets run:
```shell
$ bazel test ...
```

## Deployment

See database deployment [instructions](/infra/db).

```shell
$ kubectl create -f java/me/horlick/helloworld/greeter.yaml
```

### Upgrade

```shell
$ ./deploy.sh v0.1.4
```
