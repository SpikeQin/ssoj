package com.ss.ssoj.common;

import java.util.List;

/**
 * 分页响应结构，字段名与前端约定保持一致：records 为数据列表，total 为总条数。
 */
public class PageResult<T> {

    private List<T> records;
    private long total;

    public PageResult() {
    }

    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
