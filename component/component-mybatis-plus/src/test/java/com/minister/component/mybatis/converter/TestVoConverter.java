package com.minister.component.mybatis.converter;

import com.minister.component.mybatis.dto.TestDto;
import com.minister.component.mybatis.vo.TestVo;
import com.minister.component.utils.converter.AbstractGenericConverter;
import lombok.extern.slf4j.Slf4j;
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
public class TestVoConverter extends AbstractGenericConverter {

    @Override
    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return new HashSet<GenericConverter.ConvertiblePair>(2) {{
            add(new GenericConverter.ConvertiblePair(TestDto.class, TestVo.class));
        }};
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source instanceof TestDto
                && (TestDto.class).equals(sourceType.getType())
                && (TestVo.class).equals(targetType.getType())) {
            return convert((TestDto) source);
        }
        return null;
    }

    private TestVo convert(TestDto source) {
        try {
            TestVo dataObject = new TestVo();
            dataObject.setOid(source.getOid());
            dataObject.setId(source.getId());
            dataObject.setName(source.getName());
            dataObject.setCreatedBy(source.getCreatedBy());
            dataObject.setCreatedDate(source.getCreatedDate());
            dataObject.setUpdatedBy(source.getUpdatedBy());
            dataObject.setUpdatedDate(source.getUpdatedDate());
            return dataObject;
        } catch (Exception e) {
            log.error("convert TestDto to TestVo fail", e);
        }
        return null;
    }

}
