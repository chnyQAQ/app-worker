package com.dah.cem.app.sc.worker.workers.report.alarms;

import com.alibaba.fastjson.annotation.JSONField;
import com.dah.cem.app.sc.worker.domain.prober.ProberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class AlarmReport {

    private String dataId;

    private String enterpriseId;

    private String gatewayId;

    private String dataType;

    private Date collectTime;

    @JSONField(serialize = false)
    private Long createTime;

    private Boolean isConnectDataSource = true;

    private String reportType;

    private List<Alarm> alarms;

    @JSONField(serialize = false)
    private Map<String, Alarm> alarmMap;

    @JSONField(serialize = false)
    private Map<String, ProberStatus> proberStatusMap;

    @JSONField(serialize = false)
    private List<ProberStatus> proberStatusList;

    public AlarmReport(String enterpriseId, String gatewayId, String dataType, boolean isConnectDataSource, String reportType) {
        this.enterpriseId = enterpriseId;
        this.gatewayId = gatewayId;
        this.dataType = dataType;
        this.isConnectDataSource = isConnectDataSource;
        this.reportType = reportType;
    }

    public List<Alarm> getAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        alarms.addAll(alarmMap.values());
        return alarms;
    }
}
