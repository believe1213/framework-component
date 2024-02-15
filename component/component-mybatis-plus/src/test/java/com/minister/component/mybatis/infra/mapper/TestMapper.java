package com.minister.component.mybatis.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minister.component.mybatis.infra.entity.TestDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;

/**
 * TestMapper
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 16:56
 */
public interface TestMapper extends BaseMapper<TestDo> {

    @Select("CREATE TABLE `cbs_identity_code_push_${name}`  (\n" +
            "  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '主键',\n" +
            "  `auth_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '授权编码',\n" +
            "  `app_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '政企号应用id',\n" +
            "  `service_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '服务id',\n" +
            "  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '推送链接',\n" +
            "  `sign` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '签名',\n" +
            "  `invoke_num` int(11) NOT NULL COMMENT '调用次数',\n" +
            "  `http_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'http状态',\n" +
            "  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '调用状态',\n" +
            "  `response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '返回内容',\n" +
            "  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '异常信息',\n" +
            "  `created_date` varchar(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '授权时间',\n" +
            "  PRIMARY KEY (`id`) USING BTREE,\n" +
            "  INDEX `idx_icpush_date_status_appid`(`created_date`, `status`, `app_id`) USING BTREE\n" +
            ") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '身份码推送授权信息' ROW_FORMAT = Dynamic;")
    void create(@Param("name") String name);

    @Select("SELECT\n" +
            "\t`table_name`\n" +
            "FROM\n" +
            "\t`information_schema`.`TABLES` \n" +
            "WHERE\n" +
            "\t`table_name` = 'cbs_identity_code_push_${name}'")
    String exists(@Param("name") String name);

    @Insert("<script>" +
            "insert into `${name}`\n" +
            "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n" +
            "\t\t`id`\n" +
            "</trim>\n" +
            "values\n" +
            "<foreach collection=\"collection\" item=\"item\" separator=\",\">\n" +
            "\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n" +
            "\t\t\t\t<if test=\"item.id != null\">\n" +
            "\t\t\t\t\t\t#{item.id,jdbcType=VARCHAR},\n" +
            "\t\t\t\t</if>\n" +
            "\t\t\t\t<if test=\"item.id == null\">\n" +
            "\t\t\t\t\t\tdefault,\n" +
            "\t\t\t\t</if>\n" +
            "\t\t</trim>\n" +
            "</foreach>" +
            "</script>")
    boolean insertBatch(@Param("collection") Collection<TestDo> col, @Param("name") String name);

}
