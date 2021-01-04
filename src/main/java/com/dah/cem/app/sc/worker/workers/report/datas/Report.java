package com.dah.cem.app.sc.worker.workers.report.datas;

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
public class Report {

    private String dataId;

    private String enterpriseId;

    private String gatewayId;

    private String dataType;

    private Date collectTime;

    @JSONField(serialize = false)
    private Long createTime;

    private Boolean isConnectDataSource = true;

    private String reportType;

    private List<Data> datas;

    @JSONField(serialize = false)
    private Map<String, Data> dataMap;

    @JSONField(serialize = false)
    private List<ProberStatus> proberStatusList;

    @JSONField(serialize = false)
    private Map<String, ProberStatus> proberStatusMap;

    public Report(String enterpriseId, String gatewayId, String dataType, boolean isConnectDataSource, String reportType) {
        this.enterpriseId = enterpriseId;
        this.gatewayId = gatewayId;
        this.dataType = dataType;
        this.isConnectDataSource = isConnectDataSource;
        this.reportType = reportType;
    }

    public List<Data> getDatas() {
        List<Data> datas = new ArrayList<>();
        datas.addAll(dataMap.values());
        return datas;
    }

}
