package pt.isel.tfm;

import org.junit.Test;

import pt.isel.tfm.mqtt.MqttSourceConnectorConfig;

public class MySourceConnectorConfigTest {
	@Test
	public void doc() {
		System.out.println(MqttSourceConnectorConfig.configuration.toRst());

//		RestService registry = new RestService("http://192.168.1.82:8081");
//		try {
//			// System.out.println(registry.getLatestVersionSchemaOnly("locations-value"));
//
//			// registry.registerSchema("{\"schema\": \"{\\\"type\\\": \\\"string\\\"}\"}",
//			// "teste-values");
//			
//			// AvroConverter c = new AvroConverter();
//			// AvroSchema s = new AvroSchema(registry.getLatestVersionSchemaOnly("locations-value"));
//
//			ProtobufSchema s = new ProtobufSchema(registry.getLatestVersion("locations-value").getSchema());
//
//			System.out.println("SCHEMA ::" + s.rawSchema());
//
//		} catch (IOException | RestClientException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}