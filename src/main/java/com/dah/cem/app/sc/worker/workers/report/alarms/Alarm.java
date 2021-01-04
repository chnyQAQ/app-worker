package com.dah.cem.app.sc.worker.workers.report.alarms;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Alarm {

    public static final String ALARM_HIGH = "alarm hi:alarm";
    public static final String ALARM_LOW = "alarm lo:alarm";
    public static final String DISABLED_ALARM = "normal:alarm";
    public static final String ALARMSIGNAL = "alarmsignal";

    private String quotaId;

    private Float value;

    private String alarmType;

    private Float threshold;

    private Date alarmTime;

}
