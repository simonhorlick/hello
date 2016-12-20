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
```

Wait for the thirdpartyresource to become available, then create the clusters.

```
$ until kubectl get thirdpartyresource | grep etcd-cluster.coreos.com; do
  sleep 1;
  done
$ kubectl create -f global-etcd-cluster.yaml
$ kubectl create -f asiaeast1c-etcd-cluster.yaml
```

Just need to write this each time a non-global cluster comes up:

```shell
$ kubectl exec etcd-global-0000 -- etcdctl --endpoints http://etcd-global:2379 set "/vt/cells/asiaeast1c" "http://etcd-asiaeast1c:2379"
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
$ gcloud compute disks delete ...
$ gsutil rm -rf gs://sh-vt-backup/test_keyspace
```
