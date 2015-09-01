package net.vicp.lylab.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public final class Caster extends NonCloneableBaseObject {

	/**
	 * String-based simple cast, from one basic type to another basic type
	 * @param originalObject
	 * @param targetClass
	 * @return
	 * Object of convert result
	 * @throws
	 * LYException If convert failed
	 */
	public static Object simpleCast(String originalString, Class<?> targetClass) {
		if(originalString == null)
			throw new NullPointerException("Parameter originalObject is null");
		if(targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if(targetClass == String.class)
			return originalString;
		
		try {
			if(targetClass == String.class)
				return originalString;
			else if(targetClass == Short.class)
				return Short.valueOf(originalString);
			else if(targetClass == Integer.class)
				return Integer.valueOf(originalString);
			else if(targetClass == Long.class)
				return Long.valueOf(originalString);
			else if(targetClass == Double.class)
				return Double.valueOf(originalString);
			else if(targetClass == Float.class)
				return Float.valueOf(originalString);
			else if(targetClass == Boolean.class)
				return Boolean.valueOf(originalString);
			else if(targetClass == Byte.class)
				return Byte.valueOf(originalString);
			else if(targetClass == Character.class)
				return Character.valueOf((originalString).charAt(0));
			else if(targetClass.getName() == "short")
				return Short.valueOf(originalString);
			else if(targetClass.getName() == "int")
				return Integer.valueOf(originalString);
			else if(targetClass.getName() == "long")
				return Long.valueOf(originalString);
			else if(targetClass.getName() == "double")
				return Double.valueOf(originalString);
			else if(targetClass.getName() == "float")
				return Float.valueOf(originalString);
			else if(targetClass.getName() == "boolean")
				return Boolean.valueOf(originalString);
			else if(targetClass.getName() == "byte")
				return Byte.valueOf(originalString);
			else if(targetClass.getName() == "char")
				return Character.valueOf((originalString).charAt(0));
			else
				throw new LYException("Unsupport target type:" + targetClass.getName());
		} catch (Exception e) {
			throw new LYException("Cast failed from String [" + originalString + "] to " + targetClass.getName(), e);
		}
	}

	/**
	 * ArrayList-based simple cast, from one {@link java.util.Collection} type to Array-based basic type
	 * @param originalArray
	 * @param targetClass
	 * @return
	 * Object of convert result
	 * @throws
	 * LYException If convert failed
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object arrayCast(List originalArray, Class targetClass) {
		if(originalArray == null)
			throw new NullPointerException("Parameter originalArray is null");
		if(targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		
		Object targetArray = null;
		try {
			if(List.class.isAssignableFrom(targetClass)) {
				targetArray = new ArrayList();
				((ArrayList) targetArray).addAll(originalArray);
			}
			else if(Set.class.isAssignableFrom(targetClass)) {
				targetArray = new HashSet();
				((HashSet) targetArray).addAll(originalArray);
			}
			else if(targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$")) {
				targetArray = Arrays.copyOf(originalArray.toArray(), originalArray.size(), targetClass);
			}
			else if(Collection.class.isAssignableFrom(targetClass)) {
				targetArray = originalArray;
			}
			
//			if(Collection.class.isAssignableFrom(targetClass)) {
//				targetArray = targetClass.newInstance();
//				((Collection) targetArray).addAll(originalArray);
//			}
			if(targetArray == null)
				throw new LYException("Unsupport target type:" + targetClass.getName());
			return targetArray;
		} catch (Exception e) {
			throw new LYException("Cast failed from ArrayList [" + originalArray + "] to " + targetClass.getName(), e);
		}
	}
	
}
