package net.vicp.lylab.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TransferQueue;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public abstract class Caster extends NonCloneableBaseObject {

	/**
	 * Convert map to Object, java.util.Map field is not supported
	 * 
	 * @param xml
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T map2Object(Class<T> instanceClass, Map<String, ?> map) {
		try {
			T owner = instanceClass.newInstance();
			Set<String> names = map.keySet();
			for (String name : names) {
				try{
					Object node = map.get(name);
					if (Map.class.isAssignableFrom(node.getClass())) {
						Method setter = Utils.getSetter(owner, name);
						if (setter == null)
							continue;
						Class<?> paramClass = setter.getParameterTypes()[0];
						Utils.setter(owner, name, map2Object(paramClass, (Map<String, ?>) node));
					}
					else
						Utils.setter(owner, name, node);
				} catch (LYException e) { }
			}
			return owner;
		} catch (Exception e) {
			throw new LYException("Convert map to Object(" + instanceClass.getName() + ") failed, reason:", e);
		}
	}

	public static boolean isBasicType(Object target) {
		if (target == null)
			throw new NullPointerException("Parameter target is null");
		Class<?> targetClass = target.getClass();
		if (targetClass == String.class)
			return true;
		else if (targetClass == Short.class)
			return true;
		else if (targetClass == Integer.class)
			return true;
		else if (targetClass == Long.class)
			return true;
		else if (targetClass == Double.class)
			return true;
		else if (targetClass == Float.class)
			return true;
		else if (targetClass == Boolean.class)
			return true;
		else if (targetClass == Byte.class)
			return true;
		else if (targetClass == Character.class)
			return true;
		else if (targetClass.getName().equals("short"))
			return true;
		else if (targetClass.getName().equals("int"))
			return true;
		else if (targetClass.getName().equals("long"))
			return true;
		else if (targetClass.getName().equals("double"))
			return true;
		else if (targetClass.getName().equals("float"))
			return true;
		else if (targetClass.getName().equals("boolean"))
			return true;
		else if (targetClass.getName().equals("byte"))
			return true;
		else if (targetClass.getName().equals("char"))
			return true;
		else
			return false;
	}

	/**
	 * Test target type is java build-in Array type
	 * 
	 * @param target
	 * @return
	 */
	public static boolean isGenericArrayType(Object target) {
		if (target == null)
			throw new NullPointerException("Parameter target is null");
		Class<?> targetClass = target.getClass();
		if (targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$") || Collection.class.isAssignableFrom(targetClass))
			return true;
		else
			return false;
	}

	/**
	 * String-based simple cast, from one basic type to another basic type.
	 * 
	 * @param originalObject
	 * @param targetClass
	 * @return Object of convert result
	 * @throws LYException
	 *             If convert failed
	 */
	public static Object simpleCast(String originalString, Class<?> targetClass) {
		if (originalString == null)
			throw new NullPointerException("Parameter originalObject is null");
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if (targetClass == String.class)
			return originalString;

		try {
			if (targetClass == String.class)
				return originalString;
			else if (targetClass == Short.class)
				return Short.valueOf(originalString);
			else if (targetClass == Integer.class)
				return Integer.valueOf(originalString);
			else if (targetClass == Long.class)
				return Long.valueOf(originalString);
			else if (targetClass == Double.class)
				return Double.valueOf(originalString);
			else if (targetClass == Float.class)
				return Float.valueOf(originalString);
			else if (targetClass == Boolean.class)
				return Boolean.valueOf(originalString);
			else if (targetClass == Byte.class)
				return Byte.valueOf(originalString);
			else if (targetClass == Character.class)
				return Character.valueOf((originalString).charAt(0));
			else if (targetClass.getName().equals("short"))
				return Short.valueOf(originalString);
			else if (targetClass.getName().equals("int"))
				return Integer.valueOf(originalString);
			else if (targetClass.getName().equals("long"))
				return Long.valueOf(originalString);
			else if (targetClass.getName().equals("double"))
				return Double.valueOf(originalString);
			else if (targetClass.getName().equals("float"))
				return Float.valueOf(originalString);
			else if (targetClass.getName().equals("boolean"))
				return Boolean.valueOf(originalString);
			else if (targetClass.getName().equals("byte"))
				return Byte.valueOf(originalString);
			else if (targetClass.getName().equals("char"))
				return Character.valueOf((originalString).charAt(0));
			else
				throw new LYException("Unsupport target basic type:" + targetClass.getName());
		} catch (Exception e) {
			throw new LYException("Cast failed from String [" + originalString + "] to " + targetClass.getName(), e);
		}
	}

	/**
	 * ArrayList-based simple cast, from one {@link java.util.Collection} type
	 * to Array-based basic type However, original sort may be changed based on
	 * the container you selected.
	 * 
	 * @param originalArray
	 * @param targetClass
	 * @return Object of convert result
	 * @throws LYException
	 *             If convert failed
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object arrayCast(List originalArray, Class targetClass) {
		if (originalArray == null)
			throw new NullPointerException("Parameter originalArray is null");
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");

		Object targetArray = null;
		try {
			if (targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$")) {
				targetArray = Arrays.copyOf(originalArray.toArray(), originalArray.size(), targetClass);
			} else if (Collection.class.isAssignableFrom(targetClass)) {
				try {
					// Generic instance, if this type could be initialized
					targetArray = targetClass.newInstance();
				} catch (Exception e) {
					// If can't initialize, it maybe an interface
					if (List.class.isAssignableFrom(targetClass))
						targetArray = new ArrayList();
					else if (SortedSet.class.isAssignableFrom(targetClass))
						targetArray = new TreeSet();
					else if (Set.class.isAssignableFrom(targetClass))
						targetArray = new HashSet();
					else if (TransferQueue.class.isAssignableFrom(targetClass))
						targetArray = new LinkedTransferQueue();
					else if (BlockingQueue.class.isAssignableFrom(targetClass))
						targetArray = new SynchronousQueue();
					else if (Deque.class.isAssignableFrom(targetClass))
						targetArray = new LinkedList();
					// If no matches
					else
						targetArray = new ArrayList();
				}
				if (targetArray == null)
					throw new LYException("Unsupport target Array type:" + targetClass.getName());
				((Collection) targetArray).addAll(originalArray);
			}
			return targetArray;
		} catch (Exception e) {
			throw new LYException("Cast failed from " + originalArray.getClass().getName() + " {" + originalArray + "} to " + targetClass.getName(), e);
		}
	}

}
