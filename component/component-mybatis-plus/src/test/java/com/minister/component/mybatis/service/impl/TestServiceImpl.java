package com.minister.component.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.minister.component.mybatis.converter.ConvertMybatisPlusUtil;
import com.minister.component.mybatis.dto.TestDto;
import com.minister.component.mybatis.infra.entity.TestDo;
import com.minister.component.mybatis.infra.mapper.TestMapper;
import com.minister.component.mybatis.service.ExtendServiceImpl;
import com.minister.component.mybatis.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TestServiceImpl
 *
 * @author QIUCHANGQING620
 * @date 2020-08-25 11:59
 */
@Service
@Slf4j
public class TestServiceImpl extends ExtendServiceImpl<TestMapper, TestDo> implements TestService {

    @Resource(name = "mvcConversionService")
    private ConversionService conversionService;

    @Override
    public Collection<TestDto> queryAll() {
        Collection<TestDo> col = this.lambdaQuery()
                .orderByAsc(TestDo::getName)
                .orderByAsc(TestDo::getCreatedDate)
                .list();
        return ConvertMybatisPlusUtil.convertCol(col, TestDo.class, TestDto.class, conversionService);
    }

    @Override
    public Collection<TestDto> customQuery() {
        QueryWrapper<TestDo> queryWrapper = new QueryWrapper<>();
        Collection<Map<String, Object>> queryResult = this.baseMapper.selectMaps(queryWrapper
                .select("oid", "id", "name", "is_del isDel", "created_by createdBy", "created_date createdDate", "updated_by updatedBy", "updated_date updatedDate")
                .orderByAsc("name", "created_date"));

        Collection<TestDo> col = queryResult.stream()
                .map(map -> new TestDo(
                                Integer.parseInt(String.valueOf(map.get("oid"))),
                                String.valueOf(map.get("id")),
                                String.valueOf(map.get("name")),
                                String.valueOf(map.get("isDel")),
                                String.valueOf(map.get("createdBy")),
                                String.valueOf(map.get("createdDate")),
                                String.valueOf(map.get("updatedBy")),
                                String.valueOf(map.get("updatedDate"))
                        )
                )
                .collect(Collectors.toList());
        return ConvertMybatisPlusUtil.convertCol(col, TestDo.class, TestDto.class, conversionService);
    }

    @Override
    public Collection<TestDto> or() {
        Collection<TestDo> col = this.lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(TestDo::getId, "3")
                        .eq(TestDo::getCreatedBy, "3"))
                .or(wrapper -> wrapper
                        .eq(TestDo::getId, "4")
                        .eq(TestDo::getId, "4"))
                .orderByDesc(TestDo::getCreatedDate)
                .orderByDesc(TestDo::getId)
                .list();
        return ConvertMybatisPlusUtil.convertCol(col, TestDo.class, TestDto.class, conversionService);
    }

    @Override
    public boolean updateBatch(Collection<TestDto> col) {
        Collection<TestDo> entityCol = ConvertMybatisPlusUtil.convertCol(col, TestDto.class, TestDo.class, conversionService);

        return this.updateBatchByWrapper(entityCol, entity -> this.lambdaQuery().eq(TestDo::getId, entity.getId()));
    }

}