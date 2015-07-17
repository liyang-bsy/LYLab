package net.vicp.lylab.utils;

import net.vicp.lylab.core.CoreDef;

public class Page {
	protected int total;
	protected int start;
	protected int limit;

	public Page() {
		this.start = 0;
		this.limit = CoreDef.DEFAULT_PAGE_SIZE;
	}

	public Page(int start, int limit) {
		if (start < 1)
			start = 1;
		if (limit < 1)
			limit = 1;
		this.start = start;
		this.limit = limit;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		if (start < 1)
			start = 1;
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		if (limit < 1)
			limit = 1;
		this.limit = limit;
	}

	public void setPageNo(int pageNo) {
		if (limit < 1)
			limit = 1;
		start = pageNo / limit + 1;
	}

	public int getPageNo() {
		return (start - 1) * limit;
	}

}
