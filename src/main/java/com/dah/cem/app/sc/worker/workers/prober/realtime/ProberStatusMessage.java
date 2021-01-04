package com.dah.cem.app.sc.worker.workers.prober.realtime;

import com.dah.cem.app.sc.worker.domain.prober.ProberStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ProberStatusMessage {

    private String code;

    private String status;

    private String subStatus;

    private String probeValue;

    private Date probeTime;

    private String gatewayCode;

    public ProberStatusMessage(String code, String status, String subStatus, String probeValue, Date probeTime, String gatewayCode) {
        this.code = code;
        this.status = status;
        this.subStatus = subStatus;
        this.probeValue = probeValue;
        this.probeTime = probeTime;
        this.gatewayCode = gatewayCode;
    }

    public ProberStatus convertToProberStatus() {
        ProberStatus proberStatus = new ProberStatus();
        proberStatus.setCode(code);
        proberStatus.setStatus(status);
        proberStatus.setSubStatus(subStatus);
        proberStatus.setProbeValue(probeValue);
        proberStatus.setProbeTime(probeTime);
        proberStatus.setGatewayCode(gatewayCode);
        return proberStatus;
    }

    @JsonIgnore
    public boolean isValid() {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        if (StringUtils.isEmpty(status)) {
            return false;
        }
        if (!(status.equals(ProberStatus.STATUS_OK) || status.equals(ProberStatus.STATUS_FAULT) || status.equals(ProberStatus.STATUS_ALARM))) {
            return false;
        }
        return probeTime != null;
    }

    public void format() {
        if (subStatus == null || subStatus.equals("unknown")) {
            subStatus = "";
        }
        if (probeValue == null) {
            probeValue = "";
        }
        if (gatewayCode == null) {
            gatewayCode = "";
        }
    }

}
