package com.minister.component.mybatis.service;

import com.minister.component.mybatis.dto.TestDto;

import java.util.Collection;

/**
 * TestService
 *
 * @author QIUCHANGQING620
 * @date 2020-08-25 11:58
 */
public interface TestService {

    public Collection<TestDto> queryAll();

    public Collection<TestDto> customQuery();

    public Collection<TestDto> or();

    public boolean updateBatch(Collection<TestDto> col);

}
