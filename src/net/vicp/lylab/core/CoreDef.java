package net.vicp.lylab.core;

/**
 * LYLab - powered by Young Lee's Lab.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.26
 * @version 1.0.0
 * 
 */
public class CoreDef {
	public static final String VERSION = "1.0.0";

	public static Object[] dock = new Object[16];
	public static final String CHARSET = "UTF-8";

	public static final double DOUBLE = 1.0;
	
	public static final int B = 1;
	public static final int KB = 1024*B;
	public static final int MB = 1024*KB;
	public static final int GB = 1024*MB;
	public static final long TB = 1024*GB;

	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;
	public static final int SIX = 6;
	public static final int SEVEN = 7;
	public static final int EIGHT = 8;
	public static final int NINE = 9;
	public static final int TEN = 10;
	public static final int ELEVEN = 11;
	public static final int TWELVE = 12;
	public static final int THIRTEEN = 13;
	public static final int FOURTEEN = 14;
	public static final int FIFTEEN = 15;
	public static final int SIXTEEN = 16;
	public static final int SEVENTEEN = 17;
	public static final int EIGHTEEN = 18;
	public static final int NINETEEN = 19;
	public static final int TWENTY = 20;
	public static final int HUNDRED = TEN*TEN;
	public static final int THOUSAND = TEN*HUNDRED;

	public static final long MILLISECOND = ONE;
	public static final long SECOND = THOUSAND*MILLISECOND;
	public static final long MINUTE = SIX*TEN*SECOND;
	public static final long HOUR = SIX*TEN*MINUTE;
	public static final long DAY = TWO*TWELVE*HOUR;
	public static final long WEEK = SEVEN*DAY;

	public static final int SIZEOF_BOOLEAN = ONE;
	public static final int SIZEOF_BYTE = ONE;
	public static final int SIZEOF_CHAR = ONE;
	public static final int SIZEOF_SHORT = TWO;
	public static final int SIZEOF_INTEGER = FOUR;
	public static final int SIZEOF_FLOAT = FOUR;
	public static final int SIZEOF_LONG = EIGHT;
	public static final int SIZEOF_DOUBLE = EIGHT;
	
	public static final long WAITING = ONE*SECOND;
	public static final long WAITING_SHORT = HUNDRED*MILLISECOND;
	public static final long WAITING_LONG = THREE*SECOND;

	public static final long INTERVAL = TWO*SECOND;					// 2 second
	public static final long INTERVAL_SHORT = SECOND/TWO;				// half second
	public static final long INTERVAL_LONG = FIVE*SECOND;				// 5 second
	
	public static final long WAITING_TOLERANCE = FIVE*MINUTE;			// 5 min
	public static final long DEFAULT_TTIMEOUT = HOUR;					// 1 hour
	public static final long DEFAULT_TASK_TTIMEOUT = TEN*MINUTE;		// 10 minutes
	
	public static final int DEFAULT_POOL_MAX_SIZE = FIVE*TEN;
	
	public static final long ONE_TIME_TASK = ZERO;
	
	public static final int SOCKET_MAX_BUFFER = TWO * KB;
	public static final int DEFAULT_SOCKET_TTIMEOUT = (int) (TWENTY*SECOND);

	public static final long DEFAULT_LYCACHE_EXPIRE_TIME = FIFTEEN*TWO*MINUTE;
	public static final long DEFAULT_LYCACHE_MEMORY_LIMITATION = ONE * GB;
	public static final double DEFAULT_LYCACHE_THRESHOLD = DOUBLE * EIGHT / TEN;
	
}
