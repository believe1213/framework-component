package com.minister.component.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TestBatchUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 11:44
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestBatch {

    @Test
    public void t() {
        Thread t = new Thread();
        // 初始化数据
        Collection<String> col = new ArrayList<>();
        int total = 3;
        int pageSize = 2;
        for (int i = 1; i <= total; i++) {
            col.add(String.valueOf(i));
        }

        int pages = total == 0 ? 1 : (total + pageSize - 1) / pageSize;
        // 向后翻页
        Batch<String> batch = new Batch<>(col, pageSize);
        Assert.assertEquals(pages, batch.getPages());
        Assert.assertEquals(0, batch.getCurrentPage());
        Assert.assertEquals(-1, batch.getStartIndex());
        int i = 0;
        while (batch.hasNext()) {
            i++;
            Collection<String> list = batch.next();
            if (!batch.hasNext()) {
                Assert.assertTrue(list.size() <= pageSize);
            } else {
                Assert.assertEquals(list.size(), pageSize);
            }
            Assert.assertEquals(i, batch.getCurrentPage());
            Assert.assertEquals((i - 1) * pageSize, batch.getStartIndex());
        }
        i++;
        Assert.assertNull(batch.next());
        Assert.assertEquals(pages + 1, batch.getCurrentPage());
        Assert.assertEquals(pageSize * pages, batch.getStartIndex());

        Assert.assertNull(batch.next());
        Assert.assertEquals(pages + 1, batch.getCurrentPage());
        Assert.assertEquals(pageSize * pages, batch.getStartIndex());

        // 向前翻页
        while (batch.hasPrevious()) {
            i--;
            Collection<String> list = batch.previous();
            if (!batch.hasNext()) {
                Assert.assertTrue(list.size() <= pageSize);
            } else {
                Assert.assertEquals(list.size(), pageSize);
            }
            Assert.assertEquals(i, batch.getCurrentPage());
            Assert.assertEquals((i - 1) * pageSize, batch.getStartIndex());
        }
        i--;
        Assert.assertNull(batch.previous());
        Assert.assertEquals(0, batch.getCurrentPage());
        Assert.assertEquals(-1, batch.getStartIndex());

        Assert.assertNull(batch.previous());
        Assert.assertEquals(0, batch.getCurrentPage());
        Assert.assertEquals(-1, batch.getStartIndex());
    }

}
