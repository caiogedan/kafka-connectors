# From MQTT to Kafka

###Example Configuration

```javascript
{
  "name": "mqtt-source-connector",
  "config": {
		"tasks.max": "1",
		"connector.class": "pt.isel.tfm.MqttSourceConnector",
		"mqtt.source.client.id": "mqtt_client_source",
		"mqtt.source.broker.uri": "ssl://192.168.10.210:8883",
		"mqtt.source.topic": "locations",
		"mqtt.source.ssl":"true",
		"mqtt.source.ssl.ca":"/certs/ca.crt",
		"mqtt.source.ssl.crt":"/certs/client.crt",
		"mqtt.source.ssl.key":"/certs/client.key",
		"destination.topic": "locations",
		"key.converter":"org.apache.kafka.connect.storage.StringConverter",
		"value.converter":"org.apache.kafka.connect.storage.StringConverter"
  }
}
```