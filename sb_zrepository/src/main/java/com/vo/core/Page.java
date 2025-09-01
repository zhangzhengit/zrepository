package com.vo.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询的结果
 *
 * @author zhangzhen
 * @date 2023年9月6日
 *
 */
public final class Page<T> {

	/**
	 * 每页的条数
	 */
	private Integer size;

	/**
	 * 当前第几页
	 */
	private Long page;

	/**
	 * 总页数
	 */
	private Long totalPage;

	/**
	 * 总条数
	 */
	private Long totalCount;

	/**
	 * 本页内容
	 */
	private List<T> list;

	public Page() {
		this.size = 0;
		this.page = 0L;
		this.totalPage = 0L;
		this.totalPage = 0L;
		this.list = new ArrayList<>();
	}

	/**
	 * 是否有下一页
	 *
	 * @return
	 *
	 */
	public boolean hasNextPage() {
		return this.page < this.totalPage;
	}

	public boolean hasPreviousPage() {
		return ((this.page - 1) * this.size) <= this.totalCount;
	}

	public boolean hasContent() {
		return this.getList().size() > 0;
	}

	public Page(final Integer size, final Long page, final Long totalPage, final Long totalCount, final List<T> listAAA) {
		this.size = size;
		this.page = page;
		this.totalPage = totalPage;
		this.totalCount = totalCount;
		this.list = listAAA;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(final Integer size) {
		this.size = size;
	}

	public Long getPage() {
		return page;
	}

	public void setPage(final Long page) {
		this.page = page;
	}

	public Long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(final Long totalPage) {
		this.totalPage = totalPage;
	}

	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(final Long totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(final List<T> list) {
		this.list = list;
	}
	
}
