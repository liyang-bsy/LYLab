package net.vicp.lylab.core.datastructure;

import java.util.Comparator;
import java.util.List;

public class Algorithm<T> {

	public int binarySearch(int[] srcArray, int des) {
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

	public int binarySearch(List<T> src, T des, Comparator<T> cmp) {
		int low = 0;
		int high = src.size() - 1;
		while (low <= high) {
			int middle = (low + high) / 2;
			int result = cmp.compare(des, src.get(middle));
			if (result == 0) {
				return middle;
			} else if (result < 0) {
				high = middle - 1;
			} else {
				low = middle + 1;
			}
		}
		return -1;
	}

}
