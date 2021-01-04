package com.dah.cem.app.sc.worker.config;

import com.dah.cem.app.sc.worker.workers.prober.realtime.ProberDataRealtimeListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;

@Configuration
public class ActiveMQConfig {

    @Value("${monitor.prober.data.activemq.brokerURL}")
    private String brokerURL;

    @Value("${active.mq.topic.name:prober.status.qmirror}")
    private String topicName;

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean(name = "proberDataConnectionFactory")
    @Primary
    public ConnectionFactory proberDataConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(proberDataConnectionFactory());
        cachingConnectionFactory.setReconnectOnException(true);
        cachingConnectionFactory.setCacheConsumers(false);
        cachingConnectionFactory.setCacheProducers(false);
        cachingConnectionFactory.setSessionCacheSize(50);
        return cachingConnectionFactory;
    }

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean
    public ProberDataRealtimeListener proberDataRealtimeListener() {
        return new ProberDataRealtimeListener();
    }

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean
    public DefaultMessageListenerContainer messageListenerContainer() {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(proberDataConnectionFactory());
        messageListenerContainer.setMessageListener(proberDataRealtimeListener());
        messageListenerContainer.setDestinationName(topicName);
        messageListenerContainer.setPubSubDomain(true);
        messageListenerContainer.setMaxConcurrentConsumers(10);
        return messageListenerContainer;
    }

}
