#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "usage: deploy.sh v0.1.1"
  exit 1
fi

VERSION=$1

./package.sh $VERSION

echo "Deploying containers"
kubectl set image deployment/greeter-deployment greeter=asia.gcr.io/sh-compute-projects/greeter_container:$VERSION --record
kubectl rollout status deployment/greeter-deployment
