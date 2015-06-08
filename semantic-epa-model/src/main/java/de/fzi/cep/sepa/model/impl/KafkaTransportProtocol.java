package de.fzi.cep.sepa.model.impl;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Namespaces({"sepa", "http://sepa.event-processing.org/sepa#",
	 "dc",   "http://purl.org/dc/terms/"})
@RdfsClass("sepa:KafkaTransportProtocol")
@Entity
public class KafkaTransportProtocol extends TransportProtocol {

	@RdfProperty("sepa:zookeeperHost")
	private String zookeeperHost;
	
	@RdfProperty("sepa:zookeeperPort")
	private int zookeeperPort;
	
	@RdfProperty("sepa:kafkaPort")
	private int kafkaPort;
	
	public KafkaTransportProtocol(String kafkaHost, int kafkaPort, String topic, String zookeeperHost, int zookeeperPort)
	{
		super(kafkaHost, topic);
		this.zookeeperHost = zookeeperHost;
		this.zookeeperPort = zookeeperPort;
		this.kafkaPort = kafkaPort;
	}
	
	public KafkaTransportProtocol()
	{
		super();
	}

	public String getZookeeperHost() {
		return zookeeperHost;
	}

	public void setZookeeperHost(String zookeeperHost) {
		this.zookeeperHost = zookeeperHost;
	}

	public int getZookeeperPort() {
		return zookeeperPort;
	}

	public void setZookeeperPort(int zookeeperPort) {
		this.zookeeperPort = zookeeperPort;
	}

	public int getKafkaPort() {
		return kafkaPort;
	}

	public void setKafkaPort(int kafkaPort) {
		this.kafkaPort = kafkaPort;
	}
	
}