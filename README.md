# hello

A hello world application.

## Architecture Overview

hello consists of an [Android](/android) frontend app and a set of backend services exposing [gRPC](http://www.grpc.io) endpoints.
As gRPC is language agnostic, the backend services may be written in any language.
To glue everything together, each service is packaged as a [Docker](https://www.docker.com) container and scheduled onto machines using [Kubernetes](http://kubernetes.io).

hello is built and tested using [Bazel](https://bazel.io).
Each commit triggers a [Jenkins](https://ci.horlick.me/job/food-trip/) buildwhich runs all unit, integration and UI tests.

### Building

To build all targets simply run:
```shell
$ bazel build ...
```

Similarly, to test all targets run:
```shell
$ bazel test ...
```

### Deployment

```shell
$ kubectl create -f java/me/horlick/helloworld/greeter.yaml
```
