package com.dah.cem.app.sc.worker.config;

import com.dah.cem.app.sc.worker.workers.netty.NettyClient;
import com.dah.cem.app.sc.worker.workers.report.actor.GatewayActorManager;
import com.dah.cem.app.sc.worker.workers.report.actor.ReportActor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class CustomBeanConfig {

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean(name = "nettyClient")
    public NettyClient nettyClient() {
        return new NettyClient();
    }

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean(name = "gatewayActorManager")
    public GatewayActorManager gatewayActorManager() {
        return new GatewayActorManager();
    }

    @ConditionalOnProperty(value = {"worker.listener"}, matchIfMissing = false)
    @Bean(name = "reportActor")
    public ReportActor reportActor() {
        return new ReportActor();
    }

}
