
# From Kafka to MQTT.

###Example Configuration

```javascript
{
  "name": "mqtt-sink-connector",
  "config": {
	  "connector.class":"pt.isel.tfm.MqttSinkConnector",
	  "tasks.max":"1",
	  "topics": "locations",
	  "mqtt.sink.client.id":"mqtt_client_sink",
	  "mqtt.sink.broker.uri": "ssl://192.168.10.210:8883",	
	  "mqtt.sink.topic":"registo",
	  "mqtt.sink.ssl":"true",
	  "mqtt.sink.ssl.ca":"/certs/ca.crt",
	  "mqtt.sink.ssl.crt":"/certs/client.crt",
	  "mqtt.sink.ssl.key":"/certs/client.key",
	  "mqtt.sink.qos":"0",
	  "key.converter":"org.apache.kafka.connect.storage.StringConverter",
	  "value.converter":"org.apache.kafka.connect.storage.StringConverter"
  }
}
```