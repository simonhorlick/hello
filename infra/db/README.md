## Database

Use [vitess.io](http://vitess.io) to run a replicated MySQL cluster on Kubernetes.

Note: This document is based off instructions here: [http://vitess.io/getting-started](http://vitess.io/getting-started/).

First install the command line client, 
```shell
$ go get github.com/youtube/vitess/go/cmd/vtctlclient
```

```shell
$ gcloud config set compute/zone asia-east1-c
$ gcloud config set project sh-compute-projects
# List the current available versions
$ gcloud container get-server-config
$ gcloud container clusters create 'default' \
    --machine-type n1-standard-1 \
    --num-nodes 5 \
    --disk-size 10 \
    --scopes storage-rw \
    --zone 'asia-east1-c' \
    --preemptible \
    --cluster-version=1.5.1
```

Vitess requires a cluster of etcd nodes for coordination.

```shell
$ kubectl create -f etcd-operator-deployment.yaml
$ kubectl create -f global-etcd-cluster.yaml
$ kubectl create -f asiaeast1c-etcd-cluster.yaml
```

```shell
$ gsutil mb gs://sh-vt-backup
```

Start the web interface and management api:
```shell
$ ./vtctld-up.sh
# In a separate terminal, launch a proxy for serving the web ui
$ kubectl proxy --port=8001
$ open http://localhost:8001/api/v1/proxy/namespaces/default/services/vtctld:web/
```

Only for Development: Create PersistentVolumes that will match up with the PersistentVolumeClaims that the PetSet requires:
```shell
$ kubectl create -f vtdataroot-pv-local.yaml # Creates local hostPath based PVs
```
Production:
```shell
# TODO(simon): Would be nice to have local-ssd type, but it's only available in 375GB/node.
```

Next we launch the actual MySQL instances.
Each instance gets its own vttablet, so they're launched in the same pod.
```shell
$ ./vttablet-petset-up.sh
```

Now list the tablets to make sure they all came up properly:
```shell
$ ./kvtctl.sh ListAllTablets asiaeast1c
asiaeast1c-0000000100 test_keyspace 0 spare 10.64.1.6:15002 10.64.1.6:3306 []
asiaeast1c-0000000101 test_keyspace 0 spare 10.64.2.5:15002 10.64.2.5:3306 []
asiaeast1c-0000000102 test_keyspace 0 spare 10.64.0.7:15002 10.64.0.7:3306 []
asiaeast1c-0000000103 test_keyspace 0 spare 10.64.1.7:15002 10.64.1.7:3306 []
asiaeast1c-0000000104 test_keyspace 0 spare 10.64.2.6:15002 10.64.2.6:3306 []
```

Pick asiaeast1c-0000000100 to be master:
```shell
$ ./kvtctl.sh InitShardMaster -force test_keyspace/0 asiaeast1c-0000000100
```

Now create our database schema:
```shell
$ ./kvtctl.sh ApplySchema -sql "$(cat ../schema/greetings.sql)" test_keyspace
$ ./kvtctl.sh GetSchema asiaeast1c-0000000100
```

Take a backup:
```shell
$ ./kvtctl.sh Backup asiaeast1c-0000000102
$ ./kvtctl.sh ListBackups test_keyspace/0
```

Start an orchestrator instance to enable automatic failover.
```shell
$ ./orchestrator-up.sh
```

Initialize Vitess Routing Schema
```shell
$ ./kvtctl.sh RebuildVSchemaGraph
```

The vtgates balance and route queries across the tablets.
This is the main entry point of clients making requests.
```shell
$  ./vtgate-up.sh
```

## Manual queries

```shell
$ ./kvtctl.sh ExecuteFetchAsDba asiaeast1c-0000000100 "SELECT * FROM greetings"
```

## Node failure

Kill the current master.
This will cause the master to come back up in a read-only state.
```shell
$ kubectl delete pod vttablet-0
```

Reparent the shard
```shell
$ ./kvtctl.sh ShardReplicationPositions test_keyspace/0
asiaeast1c-0000000100 test_keyspace 0 master 10.0.2.8:15002 10.0.2.8:3306 [] MySQL56/f36f1cdb-c436-11e6-bce5-0a580a000205:1-12 0
asiaeast1c-0000000102 test_keyspace 0 replica 10.0.4.7:15002 10.0.4.7:3306 [] MySQL56/f36f1cdb-c436-11e6-bce5-0a580a000205:1-12 0
asiaeast1c-0000000101 test_keyspace 0 replica 10.0.0.6:15002 10.0.0.6:3306 [] MySQL56/f36f1cdb-c436-11e6-bce5-0a580a000205:1-12 0

$ ./kvtctl.sh EmergencyReparentShard -keyspace_shard=test_keyspace/0 -new_master=asiaeast1c-0000000102
```

But now `asiaeast1c-0000000100` disappears.

Sometimes reparenting through orchestrator fails to set read/write on the new master.

```shell
# master
$ ./kvtctl.sh SetReadWrite asiaeast1c-0000000100
# slave 1, first manually reset the tablet's master to the current shard master then restart replication on the tablet.
$ ./kvtctl.sh ReparentTablet asiaeast1c-0000000101
$ ./kvtctl.sh StartSlave asiaeast1c-0000000101
# slave 2
$ ./kvtctl.sh ReparentTablet asiaeast1c-0000000102
$ ./kvtctl.sh StartSlave asiaeast1c-0000000102

```

## Resizing storage

First change the PV size using the gcloud tool, then ssh onto the instance the PV is mounted on to resize it.
```shell
gcloud compute disks resize [DISK_NAME] --size [DISK_SIZE]
gcloud compute ssh [NODE]
sudo resize2fs /dev/disk/by-id/google-[DISK_NAME]
```

## Turning down cluster

```shell
$ gcloud container clusters delete default --zone 'asia-east1-c'
# Clean up persistent volumes:
$ gcloud compute disks list
$ gcloud compute disks delete gke-default-0b538a09-d-pvc-3817f217-c133-11e6-9352-42010af00004 gke-default-0b538a09-d-pvc-3820da3e-c133-11e6-9352-42010af00004 gke-default-0b538a09-d-pvc-3824ae06-c133-11e6-9352-42010af00004
$ gsutil rm -rf gs://sh-vt-backup/test_keyspace
```

# Testing

Using orchestrator:
* Delete replica causes kube to reschedule and it rejoins and begins serving.
* Delete master:

Firstly, clients get these errors during master outage:
```
Dec 18, 2016 10:18:50 PM io.grpc.internal.SerializingExecutor$TaskRunner run
SEVERE: Exception while executing runnable io.grpc.internal.ServerImpl$JumpToApplicationThreadServerStreamListener$2@32463f18
java.lang.RuntimeException: java.sql.SQLNonTransientException: Vitess RPC error: code: INTERNAL_ERROR
message: "target: test_keyspace.0.master, used tablet: (alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER ), no connection for key 10.0.2.5,grpc:16002,mysql:3306,vt:15002 tablet alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER , vtgate: http://vtgate-asiaeast1c-2j33g:15001/"

  at me.horlick.helloworld.GreetingsRepositoryMySQL.insertGreet(GreetingsRepositoryMySQL.java:53)
    at me.horlick.helloworld.GreeterService.sayHello(GreeterService.java:21)
    at me.horlick.helloworld.GreeterGrpc$MethodHandlers.invoke(GreeterGrpc.java:278)
    at io.grpc.stub.ServerCalls$1$1.onHalfClose(ServerCalls.java:150)
    at io.grpc.PartialForwardingServerCallListener.onHalfClose(PartialForwardingServerCallListener.java:48)
    at io.grpc.ForwardingServerCallListener.onHalfClose(ForwardingServerCallListener.java:38)
    at io.grpc.internal.ServerCallImpl$ServerStreamListenerImpl.halfClosed(ServerCallImpl.java:260)
    at io.grpc.internal.ServerImpl$JumpToApplicationThreadServerStreamListener$2.runInContext(ServerImpl.java:503)
    at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:52)
    at io.grpc.internal.SerializingExecutor$TaskRunner.run(SerializingExecutor.java:154)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
    at java.lang.Thread.run(Thread.java:745)
  Caused by: java.sql.SQLNonTransientException: Vitess RPC error: code: INTERNAL_ERROR
  message: "target: test_keyspace.0.master, used tablet: (alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER ), no connection for key 10.0.2.5,grpc:16002,mysql:3306,vt:15002 tablet alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER , vtgate: http://vtgate-asiaeast1c-2j33g:15001/"

    at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
    at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
    at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
    at com.youtube.vitess.client.SQLFuture.mapException(SQLFuture.java:107)
    at com.youtube.vitess.client.SQLFuture.checkedGet(SQLFuture.java:58)
    at com.youtube.vitess.client.VTGateBlockingTx.execute(VTGateBlockingTx.java:31)
    at me.horlick.helloworld.GreetingsRepositoryMySQL.insertGreet(GreetingsRepositoryMySQL.java:46)
    ... 12 more
    Caused by: java.util.concurrent.ExecutionException: java.sql.SQLNonTransientException: Vitess RPC error: code: INTERNAL_ERROR
    message: "target: test_keyspace.0.master, used tablet: (alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER ), no connection for key 10.0.2.5,grpc:16002,mysql:3306,vt:15002 tablet alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER , vtgate: http://vtgate-asiaeast1c-2j33g:15001/"

      at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:476)
    at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:455)
    at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:79)
    at com.google.common.util.concurrent.ForwardingFuture.get(ForwardingFuture.java:63)
    at com.youtube.vitess.client.SQLFuture.checkedGet(SQLFuture.java:51)
    ... 14 more
    Caused by: java.sql.SQLNonTransientException: Vitess RPC error: code: INTERNAL_ERROR
    message: "target: test_keyspace.0.master, used tablet: (alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER ), no connection for key 10.0.2.5,grpc:16002,mysql:3306,vt:15002 tablet alias:<cell:\"asiaeast1c\" uid:101 > hostname:\"10.0.2.5\" ip:\"10.0.2.5\" port_map:<key:\"grpc\" value:16002 > port_map:<key:\"mysql\" value:3306 > port_map:<key:\"vt\" value:15002 > keyspace:\"test_keyspace\" shard:\"0\" type:MASTER , vtgate: http://vtgate-asiaeast1c-2j33g:15001/"

      at com.youtube.vitess.client.Proto.checkError(Proto.java:67)
    at com.youtube.vitess.client.VTGateTx$1.apply(VTGateTx.java:91)
    at com.youtube.vitess.client.VTGateTx$1.apply(VTGateTx.java:87)
    at com.google.common.util.concurrent.Futures$AsyncChainingFuture.doTransform(Futures.java:1442)
    at com.google.common.util.concurrent.Futures$AsyncChainingFuture.doTransform(Futures.java:1433)
    at com.google.common.util.concurrent.Futures$AbstractChainingFuture.run(Futures.java:1408)
    at com.google.common.util.concurrent.MoreExecutors$DirectExecutor.execute(MoreExecutors.java:456)
    at com.google.common.util.concurrent.AbstractFuture.executeListener(AbstractFuture.java:817)
    at com.google.common.util.concurrent.AbstractFuture.complete(AbstractFuture.java:753)
    at com.google.common.util.concurrent.AbstractFuture.set(AbstractFuture.java:613)
    at com.google.common.util.concurrent.Futures$AbstractCatchingFuture.run(Futures.java:778)
    at com.google.common.util.concurrent.MoreExecutors$DirectExecutor.execute(MoreExecutors.java:456)
    at com.google.common.util.concurrent.AbstractFuture.executeListener(AbstractFuture.java:817)
    at com.google.common.util.concurrent.AbstractFuture.complete(AbstractFuture.java:753)
    at com.google.common.util.concurrent.AbstractFuture.set(AbstractFuture.java:613)
    at io.grpc.stub.ClientCalls$GrpcFuture.set(ClientCalls.java:461)
    at io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:440)
    at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.close(ClientCallImpl.java:481)
    at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.access$600(ClientCallImpl.java:398)
    at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:513)
    ... 5 more
```

original master:
asiaeast1c-0000000100 test_keyspace 0 master 10.0.4.5:15002 10.0.4.5:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-1
1e6-bf82-0a580a000205:1-15 0
asiaeast1c-0000000102 test_keyspace 0 replica 10.0.1.7:15002 10.0.1.7:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-
11e6-bf82-0a580a000205:1-15 0
asiaeast1c-0000000101 test_keyspace 0 replica 10.0.2.7:15002 10.0.2.7:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-
11e6-bf82-0a580a000205:1-15 0

after, original master is re-elected:
asiaeast1c-0000000102 test_keyspace 0 master 10.0.1.7:15002 10.0.1.7:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-1
1e6-bf82-0a580a000205:1-15,5dfae660-c52a-11e6-bfc1-0a580a000105:1 0
asiaeast1c-0000000100 test_keyspace 0 replica 10.0.4.8:15002 10.0.4.8:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-
11e6-bf82-0a580a000205:1-15,5dfae660-c52a-11e6-bfc1-0a580a000105:1 0
asiaeast1c-0000000101 test_keyspace 0 replica 10.0.2.7:15002 10.0.2.7:3306 [] MySQL56/24c49575-c52a-11e6-be6f-0a580a000405:1-4,4217cbb2-c52a-
11e6-bf82-0a580a000205:1-15,5dfae660-c52a-11e6-bfc1-0a580a000105:1 0

### etcd

etcd failures are common and not self healing. We need some way of managing them so they come back up cleanly.

TODO(simon): Modify etcd-up script to create statefulsets. or use the etcd operator.
