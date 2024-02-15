package com.minister.component.utils;

import com.minister.component.utils.verdict.VerdictUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TestVerdictUtil
 *
 * @author QIUCHANGQING620
 * @date 2021-12-06 10:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestVerdict {

    @Test
    public void t1() {
        VerdictUtil.load(new TestEntity("case 1", "case 2"))
                .handle(e -> "case 1".equals(e.getA()))
                .consumer(e -> System.out.println(e.getA()))
                .handle(e -> "case 2".equals(e.getB()))
                .consumer(e -> System.out.println(e.getB()))
                .handle(e -> "other".equals(e.getB()))
                .consumer(e -> System.out.println("error"))
                .last(e -> System.out.println("default"));
    }

    @AllArgsConstructor
    @Data
    public static class TestEntity {
        private String a;

        private String b;
    }

}
