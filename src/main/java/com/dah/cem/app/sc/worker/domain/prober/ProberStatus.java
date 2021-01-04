package com.dah.cem.app.sc.worker.domain.prober;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProberStatus {

    public static final String STATUS_OK = "ok";
    public static final String STATUS_FAULT = "fault";
    public static final String STATUS_ALARM = "alarm";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_ALARM_HIGH = "alarmHigh";
    public static final String STATUS_ALARM_LOW = "alarmLow";
    public static final String STATUS_ALARM_ALARMSIGNAL = "alarmSignal";

    private String id;

    private String code;

    private String status;

    private String subStatus;

    private String probeValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date probeTime;

    private String gatewayCode;

    @Override
    public String toString() {
        return "{code: " + (code == null ? "null" : code) + ", status: " + (status == null ? "null" : status) + ", subStatus: " + (subStatus == null ? "null" : subStatus) + ", value: " + (probeValue == null ? "null" : probeValue) + ", probeTime: " + (probeTime == null ? "null" : probeTime.getTime())
                + "}";
    }
}
