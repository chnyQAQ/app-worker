package com.dah.cem.app.sc.worker.workers.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dah.cem.app.sc.worker.workers.report.Flag;
import com.dah.cem.app.sc.worker.workers.report.ReportProperties;
import com.dah.cem.app.sc.worker.workers.report.alarms.AlarmReport;
import com.dah.cem.app.sc.worker.workers.report.datas.Report;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class NettyClient implements InitializingBean {

    private final EventLoopGroup loop = new NioEventLoopGroup();
    private final ReportHandler handler = new ReportHandler(this);
    @Autowired
    private ReportProperties reportProperties;

    @Override
    public void afterPropertiesSet() {
        createBootstrap(new Bootstrap(), loop);
    }

    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup group) {
        if (bootstrap != null) {
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    ByteBuf delimiter = Unpooled.copiedBuffer("\n".getBytes(MessageBuilder.DEFAULT_ENCODING));
                    //使用换行符作为SOCKET间隔符
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                    //目前只支持String编码
                    pipeline.addLast("encoder", new StringEncoder());
                    //应答数据需使用String解码
                    pipeline.addLast("decoder", new StringDecoder());
                    //对响应数据的处理
                    pipeline.addLast("handler", handler);
                }
            });
            bootstrap.remoteAddress(reportProperties.getIp(), reportProperties.getPort());
            bootstrap.connect().addListener(new ConnectionListener(this));
        }
        return bootstrap;
    }

    public void transferReport(Report report, Flag flag) {
        try {
            String data = JSON.toJSONStringWithDateFormat(report, "yyyyMMddHHmmss", SerializerFeature.WriteDateUseDateFormat);
            handler.send(report.getEnterpriseId(), data, reportProperties.getKey(), reportProperties.getIv(), flag);
        } catch (Exception e) {
            log.error("发送失败", e);
        }
    }

    public void transferAlarm(AlarmReport alarmReport, Flag flag) {
        try {
            String data = JSON.toJSONStringWithDateFormat(alarmReport, "yyyyMMddHHmmss", SerializerFeature.WriteDateUseDateFormat);
            handler.send(alarmReport.getEnterpriseId(), data, reportProperties.getKey(), reportProperties.getIv(), flag);
        } catch (Exception e) {
            log.error("发送失败。" + e);
        }
    }

}
