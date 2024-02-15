package com.minister.component.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minister.component.mybatis.converter.ConvertMybatisPlusUtil;
import com.minister.component.mybatis.dto.TestDto;
import com.minister.component.mybatis.infra.entity.TestDo;
import com.minister.component.mybatis.infra.mapper.TestMapper;
import com.minister.component.mybatis.service.TestService;
import com.minister.component.mybatis.vo.TestVo;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.converter.ConvertUtil;
import com.minister.component.utils.entity.PageVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * TestMybatisPlus
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 13:37
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMybatisApplication.class)
public class TestMybatisPlus {

    @Resource
    private TestMapper testMapper;

    @Resource(name = "mvcConversionService")
    private ConversionService conversionService;

    @Resource
    private TestService testService;

    @Test
    public void t() {
        List<TestDo> testDoList = testMapper.selectList(null);
        System.out.println(JacksonUtil.bean2Json(testDoList));

        TestDto testDto = ConvertUtil.convertObj(testDoList.get(0), TestDto.class, conversionService);
        System.out.println(testDto);

        Collection<TestDto> testDtoCol = ConvertUtil.convertCol(testDoList, TestDo.class, TestDto.class, conversionService);
        System.out.println(JacksonUtil.bean2Json(testDtoCol));

        Page<TestDo> testDoPage = new Page<>(2, 2, false);
        IPage<TestDo> testDoIPage = testMapper.selectPage(testDoPage, null);
        System.out.print(testDoIPage.getTotal() + "\n");
        System.out.print(testDoIPage.getPages() + "\n");
        System.out.println(JacksonUtil.bean2Json(testDoIPage));

        PageVo<TestDto> testDtoPageVo = ConvertMybatisPlusUtil.convertVo(testDoIPage, TestDo.class, TestDto.class, conversionService);
        System.out.println(JacksonUtil.bean2Json(testDtoPageVo));

        PageVo<TestVo> testVoPageVo = ConvertUtil.convertPageVo(testDtoPageVo, TestDto.class, TestVo.class, conversionService);
        System.out.println(JacksonUtil.bean2Json(testVoPageVo));

        // test base service
        testService.queryAll();
        // test custom
        testService.customQuery();
    }

    @Test
    public void t1() {
//        System.out.println("-------");
//        System.out.println(testMapper.exists("20200812"));
//        try {
//            testMapper.create("20200812");
//        } catch (BadSqlGrammarException e) {
//            System.out.println("exists");
//        }
//
//        System.out.println(testMapper.exists("20200812"));
    }

    @Test
    public void t2() {
//        Collection<TestDo> testDoCol1 = new ArrayList<>();
//        TestDo entity1 = new TestDo();
//        entity1.setId("test1");
//        testDoCol1.add(entity1);
//        System.out.println(testMapper.insertBatch(testDoCol1, "component_test"));
//
//        Collection<TestDo> testDoCol2 = new ArrayList<>();
//        TestDo entity2 = new TestDo();
//        entity2.setId("test2");
//        TestDo entity3 = new TestDo();
//        entity3.setId("test3");
//        testDoCol2.add(entity2);
//        testDoCol2.add(entity3);
//        System.out.println(testMapper.insertBatch(testDoCol2, "component_test"));
    }

    @Test
    public void t3() {
//        testService.or();
    }

    @Test
    public void t4() {
//        Collection<TestDto> testDtoCol = testService.queryAll();
//
//        AtomicReference<Date> nowDate = new AtomicReference<>(new Date());
//
//        testDtoCol = testDtoCol.stream()
//                .peek(dto -> {
//                    nowDate.set(DateUtil.offsetDay(nowDate.get(), -1));
//                    dto.setUpdatedDate(nowDate.get());
//                })
//                .collect(Collectors.toList());
//
//        testService.updateBatch(testDtoCol);
    }

}