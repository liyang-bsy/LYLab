package net.vicp.lylab.utils;

public class Algorithm {
	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(char[] source, char[] compare) {
		int[] next = _KMPNext(compare);
		int i = 0;
		int j = 0;
		while (i <= source.length - 1 && j <= compare.length - 1) {
			if (j == -1 || source[i] == compare[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}
		if (j < compare.length) {
			return -1;
		} else
			return i - compare.length; // 返回模式串在主串中的头下标
	}
public static void main(String[] args) {
	byte[] source = new byte[]{0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6};
	byte[] compare = new byte[]{0,1};
	System.out.println(KMPSearch(source, compare, 16));
}
	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @param offset
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(byte[] source, byte[] compare, int srcOffset) {
		int[] next = _KMPNext(compare);
		int i = srcOffset;
		int j = 0;
		while (i <= source.length - 1 && j <= compare.length - 1) {
			if (j == -1 || source[i] == compare[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}
		if (j < compare.length) {
			return -1;
		} else
			return i - compare.length - srcOffset; // 返回模式串在偏移子串中的下标
	}

	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(byte[] source, byte[] compare) {
		return KMPSearch(source, compare, 0);
	}

	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(int[] source, int[] compare) {
		int[] next = _KMPNext(compare);
		int i = 0;
		int j = 0;
		while (i <= source.length - 1 && j <= compare.length - 1) {
			if (j == -1 || source[i] == compare[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}
		if (j < compare.length) {
			return -1;
		} else
			return i - compare.length; // 返回模式串在主串中的头下标
	}

	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(long[] source, long[] compare) {
		int[] next = _KMPNext(compare);
		int i = 0;
		int j = 0;
		while (i <= source.length - 1 && j <= compare.length - 1) {
			if (j == -1 || source[i] == compare[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}
		if (j < compare.length) {
			return -1;
		} else
			return i - compare.length; // 返回模式串在主串中的头下标
	}

	/**
	 * KMP Search, O(n+m)
	 * 
	 * @param source
	 * @param compare
	 * @return return index or -1 means not found
	 */
	public static int KMPSearch(boolean[] source, boolean[] compare) {
		int[] next = _KMPNext(compare);
		int i = 0;
		int j = 0;
		while (i <= source.length - 1 && j <= compare.length - 1) {
			if (j == -1 || source[i] == compare[j]) {
				i++;
				j++;
			} else {
				j = next[j];
			}
		}
		if (j < compare.length) {
			return -1;
		} else
			return i - compare.length; // 返回模式串在主串中的头下标
	}

	private static int[] _KMPNext(char[] compare) {
		int[] next = new int[compare.length];
		next[0] = -1;
		int i = 0;
		int j = -1;
		while (i < compare.length - 1) {
			if (j == -1 || compare[i] == compare[j]) {
				i++;
				j++;
				if (compare[i] != compare[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
		}
		return next;
	}

	private static int[] _KMPNext(byte[] compare) {
		int[] next = new int[compare.length];
		next[0] = -1;
		int i = 0;
		int j = -1;
		while (i < compare.length - 1) {
			if (j == -1 || compare[i] == compare[j]) {
				i++;
				j++;
				if (compare[i] != compare[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
		}
		return next;
	}

	private static int[] _KMPNext(int[] compare) {
		int[] next = new int[compare.length];
		next[0] = -1;
		int i = 0;
		int j = -1;
		while (i < compare.length - 1) {
			if (j == -1 || compare[i] == compare[j]) {
				i++;
				j++;
				if (compare[i] != compare[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
		}
		return next;
	}

	private static int[] _KMPNext(long[] compare) {
		int[] next = new int[compare.length];
		next[0] = -1;
		int i = 0;
		int j = -1;
		while (i < compare.length - 1) {
			if (j == -1 || compare[i] == compare[j]) {
				i++;
				j++;
				if (compare[i] != compare[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
		}
		return next;
	}

	private static int[] _KMPNext(boolean[] compare) {
		int[] next = new int[compare.length];
		next[0] = -1;
		int i = 0;
		int j = -1;
		while (i < compare.length - 1) {
			if (j == -1 || compare[i] == compare[j]) {
				i++;
				j++;
				if (compare[i] != compare[j]) {
					next[i] = j;
				} else {
					next[i] = next[j];
				}
			} else {
				j = next[j];
			}
		}
		return next;
	}

}
