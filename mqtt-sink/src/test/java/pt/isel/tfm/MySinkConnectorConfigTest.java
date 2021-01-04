package pt.isel.tfm;

import org.junit.Test;

import pt.isel.tfm.mqtt.MqttSinkConnectorConfig;

public class MySinkConnectorConfigTest {
	@Test
	public void doc() {
		System.out.println(MqttSinkConnectorConfig.CONFIG.toRst());
	}
}
