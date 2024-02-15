package com.minister.component.mybatis.infra.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TestDo
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 16:48
 */
@TableName(value = "component_test")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDo {

    private Integer oid;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

    private String isDel;

    private String createdBy;

    private String createdDate;

    private String updatedBy;

    private String updatedDate;

}
