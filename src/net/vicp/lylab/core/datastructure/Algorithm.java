package net.vicp.lylab.core.datastructure;

public class Algorithm {
	public static int binarySearch(int[] srcArray, int des) {
		int low = 0;
		int high = srcArray.length - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			if (des == srcArray[middle]) {
				return middle;
			} else if (des < srcArray[middle]) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}
	
//	private class PoolKeySort implements Comparator<Pair<Long, Boolean>> {
//	public int compare(Pair<Long, Boolean> o1, Pair<Long, Boolean> o2) {
//		boolean b1 = o1.getRight() != null ? o1.getRight() : true;
//		boolean b2 = o2.getRight() != null ? o2.getRight() : true;
//		long l1 = o1.getLeft() != null ? o1.getLeft() : Long.MAX_VALUE;
//		long l2 = o2.getLeft() != null ? o2.getLeft() : Long.MAX_VALUE;
//		int ret = (b1 == b2 ? 0 : (b1 ? 1 : -1));
//		if (ret != 0)
//			return ret;
//		if (l1 > l2)
//			return 1;
//		if (l1 <= l2)
//			return -1;
//		return 0;
//	}
//}
	
}
