package com.minister.component.utils.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 分页返回Vo
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 00:51
 */
@Getter
public class PageVo<E> {

    /**
     * 页码，从1开始
     */
    private final int currentPage;

    /**
     * 页面大小
     */
    private final int pageSize;

    /**
     * 起始行
     */
    private final int startRow;

    /**
     * 总数
     */
    private final int total;

    /**
     * 总页数
     */
    private final int pages;

    /**
     * 当前页中存放的数据
     */
    private final Collection<E> records;

    /**
     * 创建空数据(无翻页信息)
     */
    public PageVo() {
        this(new ArrayList<>());
    }

    /**
     * 创建数据(无翻页信息)
     */
    public PageVo(Collection<E> records) {
        this.records = records;
        this.currentPage = 1;
        this.pageSize = 0;
        this.startRow = 0;
        this.total = -1;
        this.pages = -1;
    }

    /**
     * 创建空数据(有翻页信息, 但是不查询总数)
     */
    public PageVo(int currentPage, int pageSize) {
        this(currentPage, pageSize, new ArrayList<>());
    }

    /**
     * 创建数据(有翻页信息, 但是不查询总数)
     */
    public PageVo(int currentPage, int pageSize, Collection<E> records) {
        this(currentPage, pageSize, -1, records);
    }

    /**
     * 创建空数据(有翻页信息)
     */
    public PageVo(int currentPage, int pageSize, int total) {
        this(currentPage, pageSize, total, new ArrayList<>());
    }

    /**
     * 创建数据(有翻页信息)
     */
    public PageVo(int currentPage, int pageSize, int total, Collection<E> records) {
        if (currentPage <= 0) {
            throw new IllegalArgumentException("The currentPage must be a positive integer");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("The pageSize must be a positive integer");
        }
        this.records = records == null ? new ArrayList<>() : records;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.startRow = (this.currentPage - 1) * this.pageSize;
        this.total = total;
        if (total == -1) {
            this.pages = -1;
        } else {
            this.pages = total < pageSize ? 1 : (total + pageSize - 1) / pageSize;
        }
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return currentPage != 1;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return currentPage < pages;
    }

}
