package com.dah.cem.app.sc.worker.workers.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectionListener implements ChannelFutureListener {

    private NettyClient client;

    public ConnectionListener(NettyClient client) {
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            log.warn("连接失败，即将重新尝试连接");
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                public void run() {
                    client.createBootstrap(new Bootstrap(), loop);
                }
            }, 1, TimeUnit.MILLISECONDS);
        } else {
            log.info("连接成功");
        }
    }
}
