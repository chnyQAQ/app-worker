package com.dah.cem.app.sc.worker.workers.report.actor;

import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.domain.prober.ProberStatus;
import com.dah.cem.app.sc.worker.workers.report.ReportProperties;
import com.dah.cem.app.sc.worker.workers.report.alarms.Alarm;
import com.dah.cem.app.sc.worker.workers.report.alarms.AlarmReport;
import com.dah.cem.app.sc.worker.workers.report.datas.Data;
import com.dah.cem.app.sc.worker.workers.report.datas.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public class GatewayActor {

    private Report report;
    private Report reportHistory;

    private AlarmReport alarmReport;
    private AlarmReport alarmReportHistory;

    private Map<String, BaseTarget> contrastMap;

    private BaseTarget baseTarget;

    private ReportActor reportActor;

    private TaskScheduler taskScheduler;

    public GatewayActor(Map<String, BaseTarget> contrastMap, BaseTarget baseTarget, ReportActor reportActor, TaskScheduler taskScheduler) {
        this.contrastMap = contrastMap;
        this.baseTarget = baseTarget;
        this.reportActor = reportActor;
        this.taskScheduler = taskScheduler;
    }

    public void initialize() {
        report = new Report(baseTarget.getUnitCode(), baseTarget.getGatewayCode(), ReportProperties.DATA_TYPE_REAL, true, ReportProperties.REALTIME_REPORT);
        reportHistory = new Report(baseTarget.getUnitCode(), baseTarget.getGatewayCode(), ReportProperties.DATA_TYPE_REAL, true, ReportProperties.CONTINUES_REPORT);
        alarmReport = new AlarmReport(baseTarget.getUnitCode(), baseTarget.getGatewayCode(), ReportProperties.DATA_TYPE_ALARM, true, ReportProperties.REALTIME_REPORT);
        alarmReportHistory = new AlarmReport(baseTarget.getUnitCode(), baseTarget.getGatewayCode(), ReportProperties.DATA_TYPE_ALARM, true, ReportProperties.CONTINUES_REPORT);
        // 自动【结束】实时报文
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (report.getCreateTime() != null && (System.currentTimeMillis() - report.getCreateTime() > baseTarget.getFrequncey() * 1000L)) {
                    reportActor.insertIntoRealBuffer(contructNewReport(report));
                    destoryReport(report);
                }
            }
        }, 1000L);

        // 自动【结束】历史报文
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (reportHistory.getCreateTime() != null && (System.currentTimeMillis() - reportHistory.getCreateTime() > baseTarget.getFrequncey() * 1000L)) {
                    reportActor.insertIntoHistoryBuffer(contructNewReport(reportHistory));
                    destoryReport(reportHistory);
                }
            }
        }, 1000L);

        // 自动【结束】实时报警报文
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (alarmReport.getCreateTime() != null && (System.currentTimeMillis() - alarmReport.getCreateTime() > baseTarget.getFrequncey() * 1000L)) {
                    reportActor.insertIntoAlarmReport(contructNewAlarmReport(alarmReport));
                    destoryAlarmReport(alarmReport);
                }
            }
        }, 1000L);

        // 自动【结束】历史报警报文
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (alarmReportHistory.getCreateTime() != null && (System.currentTimeMillis() - alarmReportHistory.getCreateTime() > baseTarget.getFrequncey() * 1000L)) {
                    reportActor.insertIntoHistoryBuffer(contructNewAlarmReport(alarmReportHistory));
                    destoryAlarmReport(alarmReportHistory);
                }
            }
        }, 1000L);
    }

    public void generateReport(ProberStatus proberStatus) {
        synchronized (this) {
            if (StringUtils.isEmpty(report.getDataId())) {
                // 报文未创建, 初始化报文
                initReport(report);
                report.setCollectTime(proberStatus.getProbeTime());
            } else if (proberStatus.getProbeTime().getTime() - report.getCollectTime().getTime() > baseTarget.getFrequncey() * 1000) {
                // 报文组装完成，放进缓冲区
                reportActor.insertIntoRealBuffer(contructNewReport(report));
                // 销毁报文
                destoryReport(report);
            }
            pendingReport(report, proberStatus);
        }
    }

    public void generateAlarmReport(ProberStatus proberStatus, boolean isNormal) {
        synchronized (this) {
            if (StringUtils.isEmpty(alarmReport.getDataId())) {
                // 报文未创建, 初始化报文
                initAlarmReport(alarmReport);
                alarmReport.setCollectTime(proberStatus.getProbeTime());
            } else if (proberStatus.getProbeTime().getTime() - alarmReport.getCollectTime().getTime() > baseTarget.getFrequncey() * 1000) {
                // 报文组装完成，直接发送
                reportActor.insertIntoAlarmReport(contructNewAlarmReport(alarmReport));
                // 销毁报文
                destoryAlarmReport(alarmReport);
            }
            pendingAlarmReport(alarmReport, proberStatus, isNormal);
        }
    }

    public void generateReportHistory(ProberStatus proberStatus) {
        synchronized (this) {
            if (StringUtils.isEmpty(reportHistory.getDataId())) {
                // 报文未创建, 初始化报文
                initReport(reportHistory);
                reportHistory.setCollectTime(proberStatus.getProbeTime());
            } else if (proberStatus.getProbeTime().getTime() - reportHistory.getCollectTime().getTime() > baseTarget.getFrequncey() * 1000) {
                // 报文组装完成，直接发送
                reportActor.insertIntoHistoryBuffer(contructNewReport(reportHistory));
                // 销毁报文
                destoryReport(reportHistory);
            }
            pendingReport(reportHistory, proberStatus);
        }
    }

    public void generateAlarmReportHistory(ProberStatus proberStatus, boolean isNormal) {
        synchronized (this) {
            if (StringUtils.isEmpty(alarmReportHistory.getDataId())) {
                // 报文未创建, 初始化报文
                initAlarmReport(alarmReportHistory);
                alarmReportHistory.setCollectTime(proberStatus.getProbeTime());
            } else if (proberStatus.getProbeTime().getTime() - alarmReportHistory.getCollectTime().getTime() > baseTarget.getFrequncey() * 1000) {
                // 报文组装完成，直接发送
                reportActor.insertIntoHistoryBuffer(contructNewAlarmReport(alarmReportHistory));
                // 销毁报文
                destoryAlarmReport(alarmReportHistory);
            }
            pendingAlarmReport(alarmReportHistory, proberStatus, isNormal);
        }
    }

    private void pendingReport(Report report, ProberStatus proberStatus) {
        Map<String, Data> dataMap = report.getDataMap();
        if (dataMap == null) {
            dataMap = new HashMap<>();
        }
        dataMap.put(contrastMap.get(proberStatus.getCode()).getTargetCode(), initData(proberStatus));
        report.setDataMap(dataMap);

        List<ProberStatus> proberStatuses = report.getProberStatusList();
        if (proberStatuses == null) {
            proberStatuses = new ArrayList<>();
        }
        proberStatuses.add(proberStatus);
        report.setProberStatusList(proberStatuses);

        Map<String, ProberStatus> proberStatusMap = report.getProberStatusMap();
        if (proberStatusMap == null) {
            proberStatusMap = new HashMap<>();
        }
        proberStatusMap.put(proberStatus.getCode(), proberStatus);
        report.setProberStatusMap(proberStatusMap);
    }

    private void pendingAlarmReport(AlarmReport alarmReport, ProberStatus proberStatus, boolean isNormal) {
        Map<String, Alarm> alarmMap = alarmReport.getAlarmMap();
        if (alarmMap == null) {
            alarmMap = new HashMap<>();
        }
        alarmMap.put(contrastMap.get(proberStatus.getCode()).getTargetCode(), initAlarm(proberStatus, isNormal));
        alarmReport.setAlarmMap(alarmMap);

        List<ProberStatus> proberStatuses = report.getProberStatusList();
        if (proberStatuses == null) {
            proberStatuses = new ArrayList<>();
        }
        proberStatuses.add(proberStatus);
        alarmReport.setProberStatusList(proberStatuses);

        Map<String, ProberStatus> proberStatusMap = alarmReport.getProberStatusMap();
        if (proberStatusMap == null) {
            proberStatusMap = new HashMap<>();
        }
        proberStatusMap.put(proberStatus.getCode(), proberStatus);
        alarmReport.setProberStatusMap(proberStatusMap);
    }

    private Data initData(ProberStatus proberStatus) {
        Data data = new Data();
        data.setQuotaId(contrastMap.get(proberStatus.getCode()).getTargetCode());
        data.setValue(StringUtils.isEmpty(proberStatus.getProbeValue()) ? null : Float.valueOf(proberStatus.getProbeValue()));
        return data;
    }

    private Alarm initAlarm(ProberStatus proberStatus, boolean isNormal) {
        Alarm alarm = new Alarm();

        //指标编码
        alarm.setQuotaId(contrastMap.get(proberStatus.getCode()).getTargetCode());
        // 采集值
        alarm.setValue(StringUtils.isEmpty(proberStatus.getProbeValue()) ? null : Float.valueOf(proberStatus.getProbeValue()));
        // 报警时间戳
        alarm.setAlarmTime(proberStatus.getProbeTime());
        // 阈值
        alarm.setThreshold(StringUtils.isEmpty(proberStatus.getProbeValue()) ? null : Float.valueOf(proberStatus.getProbeValue()));

        // 报警
        if(proberStatus.getStatus().equals(ProberStatus.STATUS_ALARM)) {
            if (proberStatus.getSubStatus().equals(ProberStatus.STATUS_ALARM_HIGH)) {
                // 报警类型 上限报警
                alarm.setAlarmType(Alarm.ALARM_HIGH);
            } else if (proberStatus.getSubStatus().equals(ProberStatus.STATUS_ALARM_LOW)) {
                // 报警类型 下限报警
                alarm.setAlarmType(Alarm.ALARM_LOW);
            } else if (proberStatus.getSubStatus().equals(ProberStatus.STATUS_ALARM_ALARMSIGNAL)) {
                // 报警类型 开关报警
                alarm.setAlarmType(Alarm.ALARMSIGNAL);
                // 阈值 1
                alarm.setThreshold(1f);
            }
        } else if (proberStatus.getStatus().equals(ProberStatus.STATUS_OK) && isNormal) {
            // 报警类型 消警
            alarm.setAlarmType(Alarm.DISABLED_ALARM);
            if (proberStatus.getSubStatus().equals(ProberStatus.STATUS_ALARM_ALARMSIGNAL)) {
                // 报警类型 开关报警 消警 阈值 0
                alarm.setThreshold(0f);
            }
        }
        return alarm;
    }

    public Report contructNewReport(Report report) {
        Report newReport = new Report();
        BeanUtils.copyProperties(report, newReport);
        return newReport;
    }

    public AlarmReport contructNewAlarmReport(AlarmReport report) {
        AlarmReport newReport = new AlarmReport();
        BeanUtils.copyProperties(report, newReport);
        return newReport;
    }

    private void initReport(Report report) {
        report.setDataId(UUID.randomUUID().toString());
        report.setCreateTime(System.currentTimeMillis());
    }

    private void initAlarmReport(AlarmReport alarmReport) {
        alarmReport.setDataId(UUID.randomUUID().toString());
        alarmReport.setCreateTime(System.currentTimeMillis());
    }

    private void destoryReport(Report report) {
        report.setDataId(null);
        report.setCreateTime(null);
        report.setCollectTime(null);
        report.setDataMap(null);
        report.setProberStatusList(null);
        report.setProberStatusMap(null);
    }

    private void destoryAlarmReport(AlarmReport alarmReport) {
        alarmReport.setDataId(null);
        alarmReport.setCreateTime(null);
        alarmReport.setCollectTime(null);
        alarmReport.setAlarmMap(null);
        alarmReport.setProberStatusList(null);
        alarmReport.setProberStatusMap(null);
    }

}
