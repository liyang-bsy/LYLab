package net.vicp.lylab.core;

public class CoreDefine {

	public static final Long WAITING = 1000L;
	public static final Long WAITING_SHORT = 100L;
	public static final Long WAITING_LONG = 3000L;

	public static final Long INTERVAL = 2000L;					// 2 second
	public static final Long INTERVAL_SHORT = 500L;				// half second
	public static final Long INTERVAL_LONG = 5000L;				// 5 second
	
	public static final Long WAITING_TOLERANCE = 5*1000L;			// 5 min
	
	public static final Long ONE_TIME_TASK = 0L;
	public static final Long MILLISECOND = 1L;
	public static final Long SECOND = 1000L*MILLISECOND;
	public static final Long MINUTE = 60L*SECOND;
	public static final Long HOUR = 60L*MINUTE;
	public static final Long DAY = 24L*HOUR;
	public static final Long WEEK = 7L*DAY;
	
}
