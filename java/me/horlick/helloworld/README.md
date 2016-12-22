# helloworld

A server that implements the Greeter service in [/protos/helloworld.proto](/protos/helloworld.proto).

The server persists the names of people that have been greeted.

## Creating a deployment
```shell
$ kubectl create -f greeter.yaml --record
# And to check:
$ kubectl get deployments
$ kubectl get rs
$ kubectl rollout status deployment/greeter-deployment
```

## Releasing a new version
First make sure it's built, this should happen through CI running (/package.sh)[/package.sh].

```shell
$ kubectl set image deployment/greeter-deployment greeter=asia.gcr.io/sh-compute-projects/greeter:v0.1.1 --record
$ kubectl rollout status deployment/greeter-deployment
Waiting for rollout to finish: 2 out of 3 new replicas have been updated...
deployment greeter-deployment successfully rolled out
$ kubectl describe deployment/greeter-deployment
```

## Rolling back to a previous version
```shell
# Check the deployment history:
$ kubectl rollout history deployment/greeter-deployment
$ kubectl rollout history deployment/greeter-deployment --revision=2 # For a detailed view
# Roll back the deployment to its previous state
$ kubectl rollout undo deployment/greeter-deployment
# Alternatively, roll back to a specific revision
$ kubectl rollout undo deployment/greeter-deployment --to-revision=2
```
