package com.dah.cem.app.sc.worker.domain.sendunit;

public class Unit {

    private String unitCode;

    private String unitName;

    private String updateTime;

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Unit() {
    }

    public Unit(String unitCode, String updateTime) {
        this.unitCode = unitCode;
        this.updateTime = updateTime;
    }
}
