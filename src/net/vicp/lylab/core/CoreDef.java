package net.vicp.lylab.core;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import net.vicp.lylab.utils.Config;

/**
 * LYLab - powered by Young Lee's Lab.
 * <br><br>
 * Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2015.06.26
 * @version 1.0.7
 * 
 */
public class CoreDef extends NonCloneableBaseObject {
	public static final String VERSION = "1.0.7";

	public static Config config = new Config();
	public static Object[] dock = new Object[16];
	
	public static final OSInfo OperationSystem = new OSInfo();
	protected static String _charset = "UTF-8";
	protected static Charset charset = Charset.forName(CoreDef._charset);
	public static CharsetEncoder charsetEncoder = charset.newEncoder();
	public static CharsetDecoder charsetDecoder = charset.newDecoder();
	public static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN? true :false;
	public static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN? true :false;
	
	public static String CHARSET() {
		return _charset;
	}

	public static void setCharset(String charset) {
		CoreDef.charset = Charset.forName(charset);
		charsetEncoder = CoreDef.charset.newEncoder();
		charsetDecoder = CoreDef.charset.newDecoder();
		CoreDef._charset = charset;
	}

	public static String rootPath;
	static {
		File file = new File("");
		rootPath = file.getAbsolutePath();
	}
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
	
	public static final double DOUBLE = 1.0;
	
	public static final int B = 1;
	public static final int KB = 1024*B;
	public static final int MB = 1024*KB;
	public static final int GB = 1024*MB;
	public static final long TB = 1024*GB;

	public static final int ZERO = 0;
	public static final double HALF = 0.5;
	public static final double QUARTER = 0.25;
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
	public static final int HUNDRED = TEN * TEN;
	public static final int THOUSAND = TEN * HUNDRED;

	public static final long MILLISECOND = ONE;
	public static final long SECOND = THOUSAND * MILLISECOND;
	public static final long MINUTE = SIX * TEN * SECOND;
	public static final long HOUR = SIX * TEN * MINUTE;
	public static final long DAY = TWO * TWELVE * HOUR;
	public static final long WEEK = SEVEN * DAY;

	public static final int SIZEOF_BOOLEAN = ONE;
	public static final int SIZEOF_BYTE = ONE;
	public static final int SIZEOF_CHAR = ONE;
	public static final int SIZEOF_SHORT = TWO;
	public static final int SIZEOF_INTEGER = FOUR;
	public static final int SIZEOF_FLOAT = FOUR;
	public static final int SIZEOF_LONG = EIGHT;
	public static final int SIZEOF_DOUBLE = EIGHT;

	public static final long WAITING = ONE * SECOND;
	public static final long WAITING_SHORT = HUNDRED * MILLISECOND;
	public static final long WAITING_LONG = THREE * SECOND;

	public static final long INTERVAL = TWO * SECOND;					// 2 second
	public static final long INTERVAL_SHORT = SECOND / TWO;				// half second
	public static final long INTERVAL_LONG = FIVE * SECOND;				// 5 second

	public static final long WAITING_TOLERANCE = TWO * MINUTE;			// 5 min
	public static final int REQUEST_TTIMEOUT = (int) MINUTE;			// 60 second

	public static final long ONE_TIME_TASK = ZERO;
	
	public static final long DEFAULT_TASK_TTIMEOUT = TEN * MINUTE;		// 10 minutes

	public static final long DEFAULT_CONTAINER_TIMEOUT = 2 * MINUTE;		// 2 min
	public static final int DEFAULT_CONTAINER_MAX_SIZE = HUNDRED;
	public static final int MASSIVE_CONTAINER_MAX_SIZE = Integer.MAX_VALUE;

	public static final int DEFAULT_PERMANENT_MAX_SIZE = HUNDRED * THOUSAND;
	public static final int DEFAULT_PERMANENT_TICK = SIX * TEN;
	public static final long DEFAULT_TERMINATE_TIMEOUT = TWO * MINUTE;
	public static final long DEFAULT_TIMTOUT_CONTROLLER_INTERVAL = MINUTE / TWO;

	public static final long DEFAULT_PERMANENT_INTERVAL = SIX * TEN * SIX * TEN;		// 60 times
	
	public static final int SOCKET_MAX_BUFFER = TWO * KB;
	public static final int DEFAULT_SOCKET_CONNECT_TTIMEOUT = (int) (MINUTE);
	public static final int DEFAULT_SOCKET_READ_TTIMEOUT = (int) (TEN * MINUTE);
	public static final int DEFAULT_SOCKET_WRITE_TTIMEOUT = (int) (TWO * MINUTE);

	public static final int DEFAULT_LYCACHE_CONTAINER_SIZE = SIXTEEN;
	public static final long DEFAULT_LYCACHE_EXPIRE_TIME = FIFTEEN * TWO * MINUTE;
	public static final long DEFAULT_LYCACHE_MEMORY_LIMITATION = ONE * GB;
	public static final double DEFAULT_LYCACHE_THRESHOLD = DOUBLE * EIGHT / TEN;

	public static final int DEFAULT_PAGE_SIZE = TWENTY;
	
}
