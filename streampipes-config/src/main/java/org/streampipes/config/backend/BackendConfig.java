/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.config.backend;


import org.streampipes.config.SpConfig;

public enum BackendConfig {
  INSTANCE;

  private SpConfig config;
  private final static String BACKEND_HOST = "backend_host";
  private final static String BACKEND_PORT = "backend_port";
  private final static String JMS_HOST = "jms_host";
  private final static String JMS_PORT = "jms_port";
  private final static String KAFKA_HOST = "kafka_host";
  private final static String KAFKA_PORT = "kafka_port";
  private final static String ZOOKEEPER_HOST = "zookeeper_host";
  private final static String ZOOKEEPER_PORT = "zookeeper_port";
  private final static String ELASTICSEARCH_HOST ="elasticsearch_host";
  private final static String ELASTICSEARCH_PORT ="elasticsearch_port";
  private final static String ELASTICSEARCH_PROTOCOL = "elasticsearch_protocol";
  private final static String IS_CONFIGURED = "is_configured";
  private final static String KAFKA_REST_HOST = "kafka_rest_host";
  private final static String KAFKA_REST_PORT = "kafka_rest_port";

  BackendConfig() {
    config = SpConfig.getSpConfig("backend");

    config.register(BACKEND_HOST, "backend", "Hostname for backend");
    config.register(BACKEND_PORT, 8082, "Port for backend");

    config.register(JMS_HOST, "activemq", "Hostname for backend service for active mq");
    config.register(JMS_PORT, 61616, "Port for backend service for active mq");
    config.register(KAFKA_HOST, "kafka", "Hostname for backend service for kafka");
    config.register(KAFKA_PORT, 9092, "Port for backend service for kafka");
    config.register(ZOOKEEPER_HOST, "zookeeper", "Hostname for backend service for zookeeper");
    config.register(ZOOKEEPER_PORT, 2181, "Port for backend service for zookeeper");
    config.register(ELASTICSEARCH_HOST, "elasticsearch", "Hostname for elasticsearch service");
    config.register(ELASTICSEARCH_PORT, 9200, "Port for elasticsearch service");
    config.register(ELASTICSEARCH_PROTOCOL, "http", "Protocol the elasticsearch service");
    config.register(IS_CONFIGURED, false, "Boolean that indicates whether streampipes is " +
            "already configured or not");
    config.register(KAFKA_REST_HOST, "kafka-rest", "The hostname of the kafka-rest module");
    config.register(KAFKA_REST_PORT, 8073, "The port of the kafka-rest module");

  }

  public String getBackendHost() {
    return config.getString(BACKEND_HOST);
  }

  public int getBackendPort() {
    return config.getInteger(BACKEND_PORT);
  }

  public String getJmsHost() {
    return config.getString(JMS_HOST);
  }

  public int getJmsPort() {
    return config.getInteger(JMS_PORT);
  }

  public String getKafkaHost() {
    return config.getString(KAFKA_HOST);
  }

  public int getKafkaPort() {
    return config.getInteger(KAFKA_PORT);
  }

  public String getKafkaUrl() {
    return getKafkaHost() + ":" + getKafkaPort();
  }

  public String getZookeeperHost() {
    return config.getString(ZOOKEEPER_HOST);
  }

  public int getZookeeperPort() {
    return config.getInteger(ZOOKEEPER_PORT);
  }

  public boolean isConfigured() {
    return config.getBoolean(IS_CONFIGURED);
  }

  public void setKafkaHost(String s) {
    config.setString(KAFKA_HOST, s);
  }

  public void setZookeeperHost(String s) {
    config.setString(ZOOKEEPER_HOST, s);
  }

  public void setJmsHost(String s) {
    config.setString(JMS_HOST, s);
  }

  public void setIsConfigured(boolean b) {
    config.setBoolean(IS_CONFIGURED, b);
  }

  public String getElasticsearchHost() {
    return config.getString(ELASTICSEARCH_HOST);
  }

  public int getElasticsearchPort() {
    return config.getInteger(ELASTICSEARCH_PORT);
  }

  public String getElasticsearchProtocol() {
    return config.getString(ELASTICSEARCH_PROTOCOL);
  }

  public String getElasticsearchURL() {
    return getElasticsearchProtocol()+ "://" + getElasticsearchHost() + ":" + getElasticsearchPort();
  }

  public String getKafkaRestHost() {
    return config.getString(KAFKA_REST_HOST);
  }

  public Integer getKafkaRestPort() {
    return config.getInteger(KAFKA_REST_PORT);
  }

  public String getKafkaRestUrl() {
    return "http://" +getKafkaRestHost() +":" +getKafkaRestPort();
  }





}
