package de.fzi.cep.sepa.sources.samples.config;

import de.fzi.cep.sepa.commons.config.ProaSenseConfig;
import de.fzi.cep.sepa.commons.config.SampleConfig;
import de.fzi.cep.sepa.model.impl.JmsTransportProtocol;
import de.fzi.cep.sepa.model.impl.KafkaTransportProtocol;
import de.fzi.cep.sepa.model.impl.TransportProtocol;

public class SampleSettings {

	public static TransportProtocol kafkaProtocol(String topicName)
	{
		KafkaTransportProtocol protocol = new KafkaTransportProtocol(
				ProaSenseConfig.kafkaHost, 
				ProaSenseConfig.kafkaPort, 
				topicName, 
				ProaSenseConfig.zookeeperHost, 
				ProaSenseConfig.zookeeperPort);
		return protocol;
	}
	
	public static TransportProtocol jmsProtocol(String topicName)
	{
		JmsTransportProtocol protocol = new JmsTransportProtocol(
				SampleConfig.jmsHost, SampleConfig.jmsPort, topicName);
		return protocol;
	}
}