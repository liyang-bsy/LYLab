package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

public class Page extends CloneableBaseObject {

	int pageNo, pageSize, index;

	public Page() {
		setPageNoAndSize(1, 20);
	}

	public Page(int pageNo, int pageSize) {
		setPageNoAndSize(pageNo, pageSize);
	}

	public void setPageNoAndSize(int pageNo, int pageSize) {
		if(pageNo < 1 || pageSize < 1)
			throw new IllegalArgumentException("Bad parameter page no/page size[" + pageNo + "/" + pageSize + "]");
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		index = (pageNo - 1) * pageSize;
	}

	public int getPageNo() {
		return pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "Page [pageNo=" + pageNo + ", pageSize=" + pageSize + "]";
	}

}
