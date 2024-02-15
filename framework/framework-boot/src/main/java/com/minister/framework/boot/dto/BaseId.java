package com.minister.framework.boot.dto;

import java.io.Serializable;

/**
 * 基础标识类
 *
 * @author QIUCHANGQING620
 * @date 2019/6/28 10:50
 */
public class BaseId implements Serializable {

    /**
     * 标识
     */
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
