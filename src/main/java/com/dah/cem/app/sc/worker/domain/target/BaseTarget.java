package com.dah.cem.app.sc.worker.domain.target;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BaseTarget {


    /**
     * 设备编码（网关配置程序配置的编码）
     */
    @NotNull
    @NotBlank
    @Length(min = 0, max = 50)
    private String probeCode;

    /**
     * 设备编码（符合部里面的标准的编码）
     */
    @NotNull
    @NotBlank
    @Length(min = 17, max = 17)
    private String equipCode;
    /**
     * 指标编码
     */
    @NotNull
    @NotBlank
    @Length(min = 22, max = 22)
    private String targetCode;

    /**
     * 网关编码
     */
    @NotNull
    @NotBlank
    @Length(min = 11, max = 11)
    private String gatewayCode;

    /**
     * 网关发送数据频率
     */
    private int frequncey;

    /**
     * 点位编码
     */
    @NotNull
    @NotBlank
    @Length(min = 12, max = 12)
    private String pointCode;

    private String pointName;

    /**
     * 企业编码
     */
    @NotNull
    @NotBlank
    @Length(min = 9, max = 9)
    private String unitCode;

    private String unitName;

}
