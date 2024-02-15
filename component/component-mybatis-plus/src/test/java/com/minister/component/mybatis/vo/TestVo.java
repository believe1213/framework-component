package com.minister.component.mybatis.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.minister.component.utils.constants.Constants;

import java.util.Date;

/**
 * TestVo
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 16:48
 */
public class TestVo {

    private Integer oid;

    private String id;

    private String name;

    private String createdBy;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = Constants.GMT_8)
    private Date createdDate;

    private String updatedBy;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = Constants.GMT_8)
    private Date updatedDate;

    public TestVo(Integer oid, String id, String name, String createdBy, Date createdDate, String updatedBy, Date updatedDate) {
        this.oid = oid;
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
    }

    public TestVo() {
    }

    public Integer getOid() {
        return oid;
    }

    public void setOid(Integer oid) {
        this.oid = oid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

}
