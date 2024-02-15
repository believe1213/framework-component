package com.minister.component.mybatis.dto;

import java.util.Date;

/**
 * TestDto
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 22:05
 */
public class TestDto {

    private Integer oid;

    private String id;

    private String name;

    private Boolean isDel;

    private String createdBy;

    private Date createdDate;

    private String updatedBy;

    private Date updatedDate;

    public TestDto(Integer oid, String id, String name, Boolean isDel, String createdBy, Date createdDate, String updatedBy, Date updatedDate) {
        this.oid = oid;
        this.id = id;
        this.name = name;
        this.isDel = isDel;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
    }

    public TestDto() {
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

    public Boolean getIsDel() {
        return isDel;
    }

    public void setIsDel(Boolean isDel) {
        this.isDel = isDel;
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
