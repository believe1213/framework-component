package com.minister.component.mybatis.converter;

import com.minister.component.mybatis.dto.TestDto;
import com.minister.component.mybatis.infra.entity.TestDo;
import com.minister.component.utils.converter.AbstractGenericConverter;
import com.minister.component.utils.enums.BoolEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * TestConverter
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 22:06
 */
@Component
@Slf4j
public class TestConverter extends AbstractGenericConverter {

    @Override
    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return new HashSet<GenericConverter.ConvertiblePair>(2) {{
            add(new GenericConverter.ConvertiblePair(TestDto.class, TestDo.class));
            add(new GenericConverter.ConvertiblePair(TestDo.class, TestDto.class));
        }};
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source instanceof TestDto
                && (TestDto.class).equals(sourceType.getType())
                && (TestDo.class).equals(targetType.getType())) {
            return convert((TestDto) source);
        } else if (source instanceof TestDo
                && (TestDo.class).equals(sourceType.getType())
                && (TestDto.class).equals(targetType.getType())) {
            return convert((TestDo) source);
        }
        return null;
    }

    private TestDo convert(TestDto source) {
        try {
            TestDo dataObject = new TestDo();
            dataObject.setOid(source.getOid());
            dataObject.setId(source.getId());
            dataObject.setName(source.getName());
            dataObject.setIsDel(BoolEnum.getStrCode(source.getIsDel()));
            dataObject.setCreatedBy(source.getCreatedBy());
            dataObject.setCreatedDate(DateFormatUtils.format(source.getCreatedDate(), "yyyyMMddHHmmss"));
            dataObject.setUpdatedBy(source.getUpdatedBy());
            dataObject.setUpdatedDate(DateFormatUtils.format(source.getUpdatedDate(), "yyyyMMddHHmmss"));
            return dataObject;
        } catch (Exception e) {
            log.error("convert TestDto to TestDo fail", e);
        }
        return null;
    }

    private TestDto convert(TestDo source) {
        try {
            return new TestDto(
                    source.getOid(),
                    source.getId(),
                    source.getName(),
                    BoolEnum.parse(source.getIsDel()),
                    source.getCreatedBy(),
                    DateUtils.parseDate(source.getCreatedDate(), "yyyyMMddHHmmss"),
                    source.getUpdatedBy(),
                    DateUtils.parseDate(source.getUpdatedDate(), "yyyyMMddHHmmss")
            );
        } catch (Exception e) {
            log.error("convert TestDo to TestDto fail", e);
        }
        return null;
    }

}
