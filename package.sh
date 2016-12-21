#!/bin/bash

# FIXME(simon): How to version these properly? Create a release script that updates them?

if [ "$#" -ne 1 ]; then
  echo "usage: package.sh v0.1.1"
  exit 1
fi

VERSION=$1

# Build docker containers
echo "Building containers"
bazel build //java/me/horlick/helloworld:greeter_container
bazel run //java/me/horlick/helloworld:greeter_container asia.gcr.io/sh-compute-projects/greeter_container:$VERSION

# Push container to registry
gcloud docker -- push asia.gcr.io/sh-compute-projects/greeter_container:$VERSION
