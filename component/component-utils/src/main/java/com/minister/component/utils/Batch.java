package com.minister.component.utils;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * 集合分页
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 09:01
 */
public class Batch<E> {

    /**
     * 页面大小
     */
    private final int pageSize;

    /**
     * 总页数
     */
    private final int pages;

    /**
     * 当前页
     */
    private int currentPage;

    /**
     * 总数
     */
    private final int total;

    /**
     * 数据
     */
    private final List<E> data;

    public Batch(Collection<E> data, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("The pageSize must be a positive integer");
        }
        this.pageSize = pageSize;
        this.data = CollectionUtils.isEmpty(data) ? new Vector<>() : new Vector<>(data);
        // 默认第0页
        this.currentPage = 0;
        this.total = this.data.size();
        this.pages = this.total < pageSize ? 1 : (this.total + pageSize - 1) / pageSize;
    }

    public boolean hasNext() {
        return this.pages > this.currentPage;
    }

    public boolean hasPrevious() {
        return this.pages != 1 && this.currentPage != 1;
    }

    public List<E> next() {
        // 最多翻到 pages+1 页
        if (this.currentPage > this.pages) {
            return null;
        }
        this.currentPage++;
        return result();
    }

    public List<E> previous() {
        // 最多翻到-1页
        if (this.currentPage < 1) {
            return null;
        } else if (this.currentPage == 1) {
            this.currentPage--;
            return null;
        }
        this.currentPage--;
        return result();
    }

    public void reset() {
        this.currentPage = 0;
    }

    public int getStartIndex() {
        return this.currentPage <= 0 ? -1 : (this.currentPage - 1) * this.pageSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getPages() {
        return this.pages;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getTotal() {
        return this.total;
    }

    public List<E> getData() {
        return this.data;
    }

    private List<E> result() {
        return CollUtil.sub(this.data, (this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize);
    }

}
