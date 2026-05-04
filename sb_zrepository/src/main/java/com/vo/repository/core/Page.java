package com.vo.repository.core;

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
	private int size;

	/**
	 * 当前第几页
	 */
	private long page;

	/**
	 * 总页数
	 */
	private long totalPage;

	/**
	 * 总条数
	 */
	private long totalCount;

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

	public Page(final int size, final long page, final long totalPage, final long totalCount, final List<T> listAAA) {
		this.size = size;
		this.page = page;
		this.totalPage = totalPage;
		this.totalCount = totalCount;
		this.list = listAAA;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public long getPage() {
		return this.page;
	}

	public void setPage(final long page) {
		this.page = page;
	}

	public long getTotalPage() {
		return this.totalPage;
	}

	public void setTotalPage(final long totalPage) {
		this.totalPage = totalPage;
	}

	public long getTotalCount() {
		return this.totalCount;
	}

	public void setTotalCount(final long totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getList() {
		return this.list; 
	}

	public void setList(final List<T> list) {
		this.list = list;
	}
	
}
