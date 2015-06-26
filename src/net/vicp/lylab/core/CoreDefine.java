package net.vicp.lylab.core;

public class CoreDefine {

	public static final long WAITING = 1000L;
	public static final long WAITING_SHORT = 100L;
	public static final long WAITING_LONG = 3000L;

	public static final long INTERVAL = 2000L;					// 2 second
	public static final long INTERVAL_SHORT = 500L;				// half second
	public static final long INTERVAL_LONG = 5000L;				// 5 second
	
	public static final long WAITING_TOLERANCE = 5*1000L;			// 5 min
	public static final long DEFAULT_TTIMEOUT = 60*60*1000L; // 1 hour
	public static final long DEFAULT_TASK_TTIMEOUT = 10*60*1000L; // 10 minutes
	
	public static final long ONE_TIME_TASK = 0L;
	public static final long MILLISECOND = 1L;
	public static final long SECOND = 1000L*MILLISECOND;
	public static final long MINUTE = 60L*SECOND;
	public static final long HOUR = 60L*MINUTE;
	public static final long DAY = 24L*HOUR;
	public static final long WEEK = 7L*DAY;
	
}
