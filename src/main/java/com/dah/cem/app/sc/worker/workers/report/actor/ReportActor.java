package com.dah.cem.app.sc.worker.workers.report.actor;

import com.dah.cem.app.sc.worker.domain.prober.ProberStatus;
import com.dah.cem.app.sc.worker.domain.prober.ProberStatusMapper;
import com.dah.cem.app.sc.worker.domain.prober.ProberStatusService;
import com.dah.cem.app.sc.worker.domain.sendunit.Unit;
import com.dah.cem.app.sc.worker.domain.sendunit.UnitService;
import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.workers.netty.NettyClient;
import com.dah.cem.app.sc.worker.workers.report.Flag;
import com.dah.cem.app.sc.worker.workers.report.alarms.AlarmReport;
import com.dah.cem.app.sc.worker.workers.report.datas.Report;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
@Setter
public class ReportActor implements InitializingBean {

    private static final int BUFFER_SIZE = 5000;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GatewayActorManager gatewayActorManager;

    @Autowired
    private NettyClient client;

    @Autowired
    private ProberStatusMapper proberStatusMapper;

    @Autowired
    private ProberStatusService proberStatusService;

    @Autowired
    private Flag flag;

    @Autowired
    private UnitService unitService;

    @Autowired
    @Qualifier("actorTaskScheduler")
    private TaskScheduler taskScheduler;

    // 使用队列特性和容量特性；不能使用阻塞特性，否则影响接入数据队列的消费速度。
    private LinkedBlockingQueue<Object> reportRealBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
    private LinkedBlockingQueue<Object> reportRealAlarmBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
    private LinkedBlockingQueue<Object> reportHistoryBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);

    private Map<String, ProberStatus> proberStatusCacheMap = new ConcurrentHashMap<>();

    private Long lastSendHistoryTime = null;

    private Long lastSendRealTime = null;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void afterPropertiesSet() {
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 历史队列没有数据
                if (reportHistoryBuffer.size() == 0) {
                    // 查询数据库，放进历史队列
                    for (ProberStatus proberStatus : proberStatusMapper.getLast(10000)) {
                        handle(proberStatus);
                    }
                    // 历史队列有数据
                    if (reportHistoryBuffer.size() > 0) {
                        // 历史队列定期发（5秒钟）
                        if (lastSendHistoryTime == null || System.currentTimeMillis() - lastSendHistoryTime > 5 * 1000L) {
                            sendHistoryReport();
                            lastSendHistoryTime = System.currentTimeMillis();
                        }
                    } else { // 历史队列没有数据
                        // 预警队列直接发
                        if (reportRealAlarmBuffer.size() > 0) {
                            sendAlarmReport();
                        }
                        calendar.setTime(new Date());
                        // 历史队列没有数据，实时队列五分整刻发(0 5 10 15 20 ...)
                        if (calendar.get(Calendar.MINUTE) % 5 == 0 && (lastSendRealTime == null || System.currentTimeMillis() - lastSendRealTime > 60 * 1000L)) {
                            sendReport();
                            lastSendRealTime = System.currentTimeMillis();
                        }
                    }
                } else {
                    if (lastSendHistoryTime == null || System.currentTimeMillis() - lastSendHistoryTime > 5 * 1000L) {
                        sendHistoryReport();
                        lastSendHistoryTime = System.currentTimeMillis();
                    }
                }
            }
        }, 5 * 1000L);

        // 定期删除数据库中的过期数据
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int count = proberStatusMapper.getCount();
                while (count > 200000) {
                    proberStatusService.removeLast(10000);
                    count = proberStatusMapper.getCount();
                }
            }
        }, 60 * 60 * 1000L);

    }


    public void sendReport() {
        Report report = null;
        while ((report = (Report) reportRealBuffer.peek()) != null) {
            client.transferReport(report, flag);
            if (flag.isSuccess()) {
                reportRealBuffer.poll();
                unitService.saveOrUpdate(new Unit(report.getEnterpriseId(), dateFormat.format(new Date())));
            } else {
                break;
            }
        }
    }

    public void sendAlarmReport() {
        AlarmReport report = null;
        while ((report = (AlarmReport) reportRealAlarmBuffer.peek()) != null) {
            client.transferAlarm(report, flag);
            if (flag.isSuccess()) {
                reportRealAlarmBuffer.poll();
                unitService.saveOrUpdate(new Unit(report.getEnterpriseId(), dateFormat.format(new Date())));
            } else {
                break;
            }
        }
    }

    public void sendHistoryReport() {
        Object object = null;
        while ((object = reportHistoryBuffer.peek()) != null) {
            List<ProberStatus> proberStatusList = null;
            if (object instanceof Report) {
                Report report = (Report) object;
                client.transferReport(report, flag);
                proberStatusList = report.getProberStatusList();

            } else if (object instanceof AlarmReport) {
                AlarmReport alarmReport = (AlarmReport) object;
                client.transferAlarm(alarmReport, flag);
                proberStatusList = alarmReport.getProberStatusList();
            }
            if (flag.isSuccess()) {
                reportHistoryBuffer.poll();
                for (ProberStatus proberStatus : proberStatusList) {
                    proberStatusService.remove(proberStatus.getId());
                }
            } else {
                break;
            }
        }
    }

    public void insertIntoAlarmReport(AlarmReport alarmReport) {
        if (reportRealAlarmBuffer.remainingCapacity() == 0) {
            AlarmReport reportTemp = null;
            while ((reportTemp = (AlarmReport) reportRealAlarmBuffer.poll()) != null) {
                for (ProberStatus proberStatus : reportTemp.getProberStatusMap().values()) {
                    proberStatusService.save(proberStatus);
                }
            }
        }
        reportRealAlarmBuffer.offer(alarmReport);
    }

    public void insertIntoRealBuffer(Report report) {
        if (reportRealBuffer.remainingCapacity() == 0) {
            Report reportTemp = null;
            while ((reportTemp = (Report) reportRealBuffer.poll()) != null) {
                for (ProberStatus proberStatus : reportTemp.getProberStatusMap().values()) {
                    proberStatusService.save(proberStatus);
                }
            }
        }
        reportRealBuffer.offer(report);
    }

    public void insertIntoHistoryBuffer(Object report) {
        if (reportHistoryBuffer.remainingCapacity() > 0) {
            reportHistoryBuffer.offer(report);
        }
    }

    private void handle(ProberStatus proberStatus) {
        BaseTarget baseTarget = gatewayActorManager.getContrastMap().get(proberStatus.getCode());
        if (baseTarget == null) {
            return;
        }
        GatewayActor actor = gatewayActorManager.getActor(baseTarget.getGatewayCode());
        if (actor != null) {
            ProberStatus cacheProberStatus = proberStatusCacheMap.get(proberStatus.getCode());
            if (proberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)) {
                // 组装报警报文
                if (cacheProberStatus == null || !cacheProberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)
                        || !cacheProberStatus.getSubStatus().equals(proberStatus.getSubStatus())) {
                    actor.generateAlarmReportHistory(proberStatus, false);
                } else {
                    proberStatusService.remove(proberStatus.getId());
                }
                // 将状态放入缓存
                proberStatusCacheMap.put(proberStatus.getCode(), proberStatus);
            } else if (proberStatus.getStatus().equals(ProberStatus.STATUS_OK)) {
                // 组装指标报文
                actor.generateReportHistory(proberStatus);
                // 组装消警报文
                if (cacheProberStatus != null && cacheProberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)) {
                    actor.generateAlarmReportHistory(proberStatus, true);
                }

                // 将状态放入缓存
                proberStatusCacheMap.put(proberStatus.getCode(), proberStatus);
            }
        }
    }
}
