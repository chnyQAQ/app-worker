package com.dah.cem.app.sc.worker.workers.report;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class ReportProperties {

    public static final String REALTIME_REPORT = "report";
    public static final String CONTINUES_REPORT = "continues";

    public static final String DATA_TYPE_REAL = "energy_data";
    public static final String DATA_TYPE_ALARM = "alarm";

    @Value("${data.report.ip}")
    private String ip;

    @Value("${data.report.port}")
    private int port;

    @Value("${data.report.key}")
    private String key;

    @Value("${data.report.iv}")
    private String iv;

}
