package com.dah.cem.app.sc.worker.workers.prober.realtime;

import com.alibaba.fastjson.JSONArray;
import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.domain.prober.ProberStatus;
import com.dah.cem.app.sc.worker.infrastructure.exception.ServiceException;
import com.dah.cem.app.sc.worker.workers.report.actor.GatewayActor;
import com.dah.cem.app.sc.worker.workers.report.actor.GatewayActorManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProberDataRealtimeListener implements MessageListener {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private GatewayActorManager gatewayActorManager;

    private Map<String, ProberStatus> proberStatusCacheMap = new ConcurrentHashMap<>();

    /**
     * 监听实时数据
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            for (ProberStatusMessage msg : convert(message)) {
                if (msg.isValid()) {
                    msg.format();
                    ProberStatus proberStatus = msg.convertToProberStatus();
                    //log.info("探测器实时状态 - " + proberStatus.toString());
                    handle(proberStatus);
                } else {
                    log.warn("探测器实时状态消息不合法 - " + msg);
                }
            }
        } catch (Throwable throwable) {
            log.error("探测器实时状态消息处理异常 - " + message, throwable);
        }
    }

    private List<ProberStatusMessage> convert(Message message) {
        // 转换
        List<ProberStatusMessage> messages = new ArrayList<>();
        if (message instanceof TextMessage) {
            messages.addAll(convertTextMessage((TextMessage) message));
        } else if (message instanceof MapMessage) {
            messages.add(convertMapMessage((MapMessage) message));
        } else if (message instanceof ObjectMessage) {
            messages.addAll(convertObjectMessage((ObjectMessage) message));
        } else if (message instanceof BytesMessage) {
            messages.add(convertBytesMessage((BytesMessage) message));
        } else {
            log.warn("探测器实时状态消息数据类型无法识别 - " + message);
        }
        return messages;
    }

    private List<ProberStatusMessage> convertTextMessage(TextMessage message) {
        try {
            String messageText = message.getText();
            if (messageText.startsWith("[")) {
                JSONArray jsonArray = JSONArray.parseArray(messageText);
                return jsonArray.toJavaList(ProberStatusMessage.class);
            } else {
                ProberStatusMessage proberStatusMessage = objectMapper.readValue(messageText, ProberStatusMessage.class);
                List<ProberStatusMessage> proberStatusMessages = new ArrayList<>();
                proberStatusMessages.add(proberStatusMessage);
                return proberStatusMessages;
            }
        } catch (Exception e) {
            throw new ServiceException("探测器实时状态消息转换失败（json） - " + message, e);
        }
    }

    private ProberStatusMessage convertMapMessage(MapMessage message) {
        try {
            String code = message.getString("proberCode");
            String status = message.getString("status");
            String subStatus = message.getString("subStatus");
            String value = message.getString("probeValue");
            String probeTime = message.getString("probeTime");
            String gatewayCode = message.getString("gatewayCode");
            return new ProberStatusMessage(code, status, subStatus, value, (StringUtils.isEmpty(probeTime) ? null : new Date(Long.valueOf(probeTime))), gatewayCode);
        } catch (Exception e) {
            throw new ServiceException("探测器实时状态消息转换失败（map） - " + message, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ProberStatusMessage> convertObjectMessage(ObjectMessage message) {
        try {
            List<ProberStatusMessage> list = new ArrayList<>();
            for (Map<String, String> map : ((List<Map<String, String>>) message.getObject())) {
                String code = map.get("code");
                String status = map.get("status");
                String subStatus = map.get("subStatus");
                String value = map.get("probeValue");
                String probeTime = map.get("probeTime");
                String gatewayCode = map.get("gatewayCode");
                list.add(new ProberStatusMessage(code, status, subStatus, value, (StringUtils.isEmpty(probeTime) ? null : new Date(Long.valueOf(probeTime))), gatewayCode));
            }
            return list;
        } catch (Exception e) {
            throw new ServiceException("探测器实时状态消息转换失败（object） - " + message, e);
        }
    }

    private ProberStatusMessage convertBytesMessage(BytesMessage message) {
        try {
            String result = "";
            byte[] bs = new byte[1024];
            BytesMessage msg = message;
            while (msg.readBytes(bs) != -1) {
                result = new String(bs);
            }
            return objectMapper.readValue(result, ProberStatusMessage.class);
        } catch (Exception e) {
            throw new ServiceException("探测器实时状态消息转换失败（bytes） - " + message, e);
        }
    }

    private void handle(ProberStatus proberStatus) {
        ProberStatus cacheProberStatus = proberStatusCacheMap.get(proberStatus.getCode());
        // 降频
        if(necessaryToSendBetweenBatches(proberStatus, cacheProberStatus)) {
            BaseTarget baseTarget = gatewayActorManager.getContrastMap().get(proberStatus.getCode());
            if (baseTarget == null) {
                return;
            }
            GatewayActor actor = gatewayActorManager.getActor(baseTarget.getGatewayCode());
            if (actor != null) {
                if (proberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)) {
                    // 组装报警报文
                    if (cacheProberStatus == null || !cacheProberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)
                            || !cacheProberStatus.getSubStatus().equals(proberStatus.getSubStatus())) {
                        actor.generateAlarmReport(proberStatus, false);
                    }
                    // 将状态放入缓存
                    proberStatusCacheMap.put(proberStatus.getCode(), proberStatus);
                } else if (proberStatus.getStatus().equals(ProberStatus.STATUS_OK)) {
                    // 组装指标报文
                    actor.generateReport(proberStatus);
                    // 组装消警报文
                    if (cacheProberStatus != null && cacheProberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)) {
                        actor.generateAlarmReport(proberStatus, true);
                    }
                    // 将状态放入缓存
                    proberStatusCacheMap.put(proberStatus.getCode(), proberStatus);
                }
            }
        }
    }

    public boolean necessaryToSendBetweenBatches(ProberStatus status, ProberStatus lastStatus) {
        if (lastStatus == null) {
            // 从未发送过，需要发送
            return true;
        } else if (status.getProbeTime().before(lastStatus.getProbeTime())) {
            // 当前记录的探测时间小于上一次的探测时间，即时间发生倒退，不发送
            return false;
        } else // 探测值合法且未发生变化，但超过时长，需要发送
            if (!status.getStatus().equals(lastStatus.getStatus()) || !status.getSubStatus().equals(lastStatus.getSubStatus()) || !status.getProbeValue().equals(lastStatus.getProbeValue())) {
            // 状态或探测值发生变化，需要发送
            return true;
        } else return status.getProbeTime().getTime() - lastStatus.getProbeTime().getTime() > (60 * 1000);
    }

}
