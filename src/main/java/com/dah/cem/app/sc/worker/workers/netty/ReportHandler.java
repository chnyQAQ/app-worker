package com.dah.cem.app.sc.worker.workers.netty;

import com.dah.cem.app.sc.worker.workers.report.Flag;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class ReportHandler extends ChannelHandlerAdapter {

    private ChannelHandlerContext channel;

    private NettyClient client;

    public ReportHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("通道激活，准备发送数据");
        channel = ctx;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        log.warn("连接中断，重新连接");
        final EventLoop eventLoop = ctx.channel().eventLoop();
        // 当连接中断时，快速反应，重新建立连接（1毫秒）
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                client.createBootstrap(new Bootstrap(), eventLoop);
            }
        }, 1, TimeUnit.MILLISECONDS);
    }

    public void send(String enterpriseId, String report, String key, String iv, Flag flag) throws Exception {
        if (this.channel == null) {
            flag.setSuccess(false);
            log.error("channel未初始化！");
        }
        channel.writeAndFlush(MessageBuilder.getSendMsg(enterpriseId, report, key, iv)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    flag.setSuccess(true);
                } else {
                    flag.setSuccess(false);
                }
            }
        });

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("应答数据：" + msg);
    }
}
