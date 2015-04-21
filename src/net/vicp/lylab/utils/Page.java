package net.vicp.lylab.utils;

public class Page {
	public static final int DEFAULT_PAGE_SIZE = 20;
	private int totalProperty;
	private int pageCount;
	private int pageSize;
	private int pageIndex;
	protected int firstPageNo = 1;
	protected int prePageNo = 1;
	protected int nextPageNo = 1;
	protected int lastPageNo = 1;

	public Page() {
		this.pageIndex = 1;
		this.pageSize = DEFAULT_PAGE_SIZE;
	}

	public Page(int start, int pageSize) {
		if (pageSize < 1)
			pageSize = 1;
		this.pageSize = pageSize;
		pageIndex = start / pageSize;
		pageIndex++;
	}

	public Page(int pageIndex) {
		if (pageIndex < 1)
			pageIndex = 1;
		this.pageIndex = pageIndex;
		this.pageSize = DEFAULT_PAGE_SIZE;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageCount() {
		return pageCount;
	}

	public int getFirstResult() {
		return (pageIndex - 1) * pageSize;
	}

	public boolean getHasPrevious() {
		return pageIndex > 1;
	}

	public boolean getHasNext() {
		return pageIndex < pageCount;
	}

	public boolean isEmpty() {
		return totalProperty == 0;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getTotalProperty() {
		return totalProperty;
	}

	public void setTotalProperty(int totalProperty) {
		this.totalProperty = totalProperty;

		pageCount = totalProperty / pageSize
				+ (totalProperty % pageSize == 0 ? 0 : 1);
		if (totalProperty == 0) {
			// if (pageIndex != 1)
			// throw new IndexOutOfBoundsException("Page index out of range.");
		} else {
			// if (pageIndex > pageCount)
			// throw new IndexOutOfBoundsException("Page index out of range.");
		}
	}
}
