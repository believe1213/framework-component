package com.minister.framework.api.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 分页查询基础对象
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 00:46
 */
@Data
public class PageQueryCmd {

    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码错误")
    private Integer currentPage;

    /**
     * 页面大小
     */
    @NotNull(message = "页面大小不能为空")
    @Range(min = 1, max = 100, message = "页面大小错误")
    private Integer pageSize;

    /**
     * 是否包含count查询[默认：不查询]
     */
    private boolean count = false;

}
