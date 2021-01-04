# From Kafka to Kafka (Replicator).

###Example Configuration

```javascript
{
  "name": "replicator-connector",
  "config": {
	  "tasks.max": "1", 
	  "connector.class": "pt.isel.tfm.ReplicatorSourceConnector",
	  "src.kafka.bootstrap.servers":"192.168.10.210:9092",
	  "replicator.group.name": "VM_1-VM_2",
	  "topic.whitelist":"vPosition",
	  "dst.kafka.bootstrap.servers": "localhost:9092",
	  "dst.kafka.topic":"vPosition",
	  "topic.replication.factor": 1,
	  "key.converter":"org.apache.kafka.connect.storage.StringConverter",
	  "value.converter":"org.apache.kafka.connect.storage.StringConverter"
  }
}
```