package net.vicp.lylab.core.model;

import net.vicp.lylab.core.CloneableBaseObject;

public class OrderBy extends CloneableBaseObject {
	String field;
	String order;

	public OrderBy(String field, String order) {
		this.field = field;
		this.order = order;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

}
