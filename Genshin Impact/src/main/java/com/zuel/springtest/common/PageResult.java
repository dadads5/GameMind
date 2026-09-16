package com.zuel.springtest.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页响应体
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> content = new ArrayList<>();
    /** 总记录数 */
    private long total;
    /** 当前页码，从 1 开始 */
    private int page;
    /** 每页条数 */
    private int size;
    /** 总页数 */
    private int totalPages;

    public PageResult() {
    }

    public PageResult(List<T> content, long total, int page, int size) {
        this.content = content == null ? new ArrayList<>() : content;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
    }

    /**
     * 由 PageHelper 的 {@link PageInfo} 转换为统一分页结果
     */
    public static <E> PageResult<E> of(PageInfo<E> pageInfo) {
        return new PageResult<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    /**
     * 分页结果的元素类型转换（DO -> VO）
     */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = new ArrayList<>(this.content.size());
        for (T item : this.content) {
            mapped.add(mapper.apply(item));
        }
        PageResult<R> result = new PageResult<>();
        result.setContent(mapped);
        result.setTotal(this.total);
        result.setPage(this.page);
        result.setSize(this.size);
        result.setTotalPages(this.totalPages);
        return result;
    }
}
