package net.vicp.lylab.core.model;

import java.util.Date;

/**
 * 时间范围工具类
 * 
 * @author liyang
 * @date 2017-03-01 17:01
 */
public class TimeRange extends ValueRange<Long> {
	public static final Date MIN_DATE_VALUE = new Date(Long.MIN_VALUE);
	public static final Date MAX_DATE_VALUE = new Date(Long.MAX_VALUE);

	public TimeRange() {
	}

	public TimeRange(Date fromDate, Date toDate) {
		if (fromDate == null)
			throw new IllegalArgumentException("Parameter startTime is null");
		if (toDate == null)
			throw new IllegalArgumentException("Parameter endTime is null");
		this.from = fromDate.getTime();
		this.to = toDate.getTime();
	}

	public TimeRange(Long from, Long to) {
		super(from, to);
	}

	public Date getFromDate() {
		return new Date(from.longValue());
	}

	public void setFromDate(Date fromDate) {
		if (fromDate == null)
			throw new IllegalArgumentException("Parameter startTime is null");
		this.from = fromDate.getTime();
	}

	public Date getToDate() {
		return new Date(to.longValue());
	}

	public void setToDate(Date toDate) {
		if (toDate == null)
			throw new IllegalArgumentException("Parameter endTime is null");
		this.to = toDate.getTime();
	}

}