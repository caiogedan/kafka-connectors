package pt.isel.tfm;

import org.junit.Test;

import pt.isel.tfm.mqtt.MqttSourceConnectorConfig;

public class MySourceConnectorConfigTest {
  @Test
  public void doc() {
    System.out.println(MqttSourceConnectorConfig.configuration.toRst());
  }
}