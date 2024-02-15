package com.minister.framework.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * 操作 基础数据Cmd
 *
 * @author QIUCHANGQING620
 * @date 2021-04-27 09:36
 */
@Data
public class BaseOperatorCmd {

    /**
     * 操作人
     */
    @NotBlank(message = "操作人不能为空")
    private String operator;

    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空")
    @JsonFormat(pattern = NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private Date operatedDate;

}
