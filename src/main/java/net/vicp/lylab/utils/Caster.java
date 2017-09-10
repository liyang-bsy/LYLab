package net.vicp.lylab.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

public abstract class Caster extends NonCloneableBaseObject {

	private static Map<String, BeanCopier> beanCopiers = new ConcurrentHashMap<String, BeanCopier>();

	/**
	 * Deep copy may cause infinite loop
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public static final <T> T beanCopy(Object source, T target) {
		return beanCopy(source, target, new Converter() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Object convert(Object value, Class target, Object context) {
				// 不转化null的值
				if (value == null)
					return null;
				return objectCast(value, target);
			}
		});
	}

	private static final <T> T beanCopy(Object source, T target, Converter converter) {
		if (source == null || target == null)
			return target;
		// always true
		boolean useConvert = converter != null;
		String beanKey = _generateKey(source.getClass(), target.getClass(), useConvert);
		BeanCopier copier = null;
		if (!beanCopiers.containsKey(beanKey)) {
			copier = BeanCopier.create(source.getClass(), target.getClass(), useConvert);
			beanCopiers.put(beanKey, copier);
		} else {
			copier = beanCopiers.get(beanKey);
		}
		copier.copy(source, target, converter);
		return target;
	}

	private static String _generateKey(Class<?> class1, Class<?> class2, boolean convert) {
		return class1.toString() + "_" + String.valueOf(convert) + "_" + class2.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T objectCast(Object source, Class<T> targetClass) {
		T target = null;
		if (source == null) {
			return target;
		} else if (source.getClass().equals(targetClass)) {
			target = (T) source;
		} else if (isBasicType(source)) {
			// basic type convert
			target = (T) simpleCast(source, targetClass);
		} else if (isGenericArrayType(source)) {
			// array convert
			target = (T) arrayTypeCast((Collection<?>) source, targetClass);
		} else if (source instanceof Map) {
			// map convert
			target = mapCastObject(targetClass, (Map<String, ?>) source);
		} else if (Map.class.isAssignableFrom(targetClass)) {
			// low performance
			target = (T) objectCastMap(source);
		} else {
			// object convert
			try {
				target = targetClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("无法新建对象用于bean copy", e);
			}
			beanCopy(source, target);
		}
		return target;
	}

	public static <T> List<T> arrayCast(Collection<?> source, Class<T> targetType) {
		List<T> list = new ArrayList<T>();
		for (Object sourceObject : source) {
			list.add(objectCast(sourceObject, targetType));
		}
		return list;
	}

	public static <T> List<T> arrayCast(T[] source, Class<T> targetType) {
		List<T> list = new ArrayList<T>();
		for (Object sourceObject : source) {
			list.add(objectCast(sourceObject, targetType));
		}
		return list;
	}

	/**
	 * Convert map to Object
	 * 
	 * @param instanceClass
	 * @param map
	 * @return
	 */
	public final static <T> T mapCastObject(Class<T> instanceClass, Map<String, ?> map) {
		return mapCastObject(instanceClass, map, new Stack<Map<String, ?>>());
	}

	/**
	 * Convert map to Object, java.util.Map field is not supported
	 * 
	 * @param instanceClass
	 * @param map
	 * @param mapStackTrace
	 *            trace path to ensure no loop reference
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private final static <T> T mapCastObject(Class<T> instanceClass, Map<String, ?> map, Stack<Map<String, ?>> mapStackTrace) {
		if (map == null)
			throw new NullPointerException("Parameter map is null");
		if (mapStackTrace.contains(map))
			return null;
		try {
			mapStackTrace.push(map);
			T owner = instanceClass.newInstance();
			Set<String> names = map.keySet();
			for (String name : names) {
				try {
					Object node = map.get(name);
					if (node == null)
						continue;
					Method setter = Utils.getSetterForField(owner, name, node);
					if (setter == null)
						continue;
					Class<?> paramClass = setter.getParameterTypes()[0];
					if (Map.class.isAssignableFrom(paramClass)) {
						Utils.setter(owner, setter, node);
					} else if (Map.class.isAssignableFrom(node.getClass())) {
						Object param = mapCastObject(paramClass, (Map<String, ?>) node, mapStackTrace);
						if (param == null)
							param = owner;
						Utils.setter(owner, setter, param);
					} else
						Utils.setter(owner, setter, node);
				} catch (Exception e) {
					log.debug("Bad field:" + name + Utils.getStringFromThrowable(e));
				}
			}
			return owner;
		} catch (Exception e) {
			throw new RuntimeException("Convert map to Object(" + instanceClass.getName() + ") failed, reason:", e);
		} finally {
			mapStackTrace.pop();
		}
	}

	/**
	 * Convert Object to map, default depthLimit(at most) is 10
	 * 
	 * @param object
	 * @return
	 */
	public final static Map<String, Object> objectCastMap(Object object) {
		return objectCastMap(object, 10);
	}

	/**
	 * Convert Object to map
	 * 
	 * @param object
	 * @param depthLimit
	 * @return
	 */
	public final static Map<String, Object> objectCastMap(Object object, int depthLimit) {
		return objectCastMap(object, depthLimit, 0);
	}

	private final static Map<String, Object> objectCastMap(Object object, int depthLimit, int depth) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Method[] methods = object.getClass().getMethods();
			for (Method method : methods) {
				if (method.getParameters().length != 0)
					continue;
				String methodName = method.getName();
				if (methodName.equals("getClass"))
					continue;
				String fieldName = null;
				if (methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3)))
					fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
				if ((methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2))))
					fieldName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
				if (fieldName != null) {
					if (!method.isAccessible()) {
						method.setAccessible(true);
					}
					Object temp = innerObjectCastMap(method.invoke(object), depthLimit, depth + 1);
					if (temp != null) {
						map.put(fieldName, temp);
					}
				}
			}
			return map;
		} catch (Exception e) {
			throw new RuntimeException("Convert Object to map(" + object + ") failed, reason:", e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final static Object innerObjectCastMap(Object object, int depthLimit, int depth) {
		if (depth > depthLimit)
			return null;
		if (object == null)
			return null;
		if (isGenericArrayType(object)) {
			ArrayList<Object> container = new ArrayList<Object>();
			for (Object item : (Collection) object) {
				Object temp = innerObjectCastMap(item, depthLimit, depth + 1);
				if (temp == null) {
					continue;
				}
				container.add(temp);
			}
			return container;
		} else if (Map.class.isAssignableFrom(object.getClass())) {
			Map<String, Object> container = new HashMap<String, Object>();
			Set<Map.Entry> entries = ((Map) object).entrySet();
			for (Map.Entry entry : entries) {
				Object temp = innerObjectCastMap(entry.getValue(), depthLimit, depth + 1);
				if (temp == null) {
					continue;
				}
				container.put(String.valueOf(entry.getKey()), temp);
			}
			return container;
		} else if (isBasicType(object)) {
			return object;
		} else {
			return objectCastMap(object, depthLimit, depth + 1);
		}
	}

	public final static boolean isBasicType(Object target) {
		if (target == null)
			throw new NullPointerException("Parameter target is null");
		return isBasicType(target.getClass());
	}

	public final static boolean isBasicType(Class<?> targetClass) {
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
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
		else if (targetClass == Date.class)
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
	public final static boolean isGenericArrayType(Object target) {
		if (target == null)
			throw new NullPointerException("Parameter target is null");
		return isGenericArrayType(target.getClass());
	}

	/**
	 * Test target type is java build-in Array type
	 * 
	 * @param targetClass
	 * @return
	 */
	public final static boolean isGenericArrayType(Class<?> targetClass) {
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if (isJavaArrayType(targetClass) || Collection.class.isAssignableFrom(targetClass))
			return true;
		else
			return false;
	}

	/**
	 * Test target type is java Array type
	 * 
	 * @param targetClass
	 * @return
	 */
	public final static boolean isJavaArrayType(Class<?> targetClass) {
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if (targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$"))
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
	 * @throws RuntimeException
	 *             If convert failed
	 */
	public final static Object simpleCast(Object original, Class<?> targetClass) {
		if (original == null)
			return null;
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if (targetClass.isAssignableFrom(original.getClass()))
			return original;
		String originalString = "";
		if (original instanceof Date) {
			originalString = String.valueOf(((Date) original).getTime());
		} else if (original instanceof Boolean) {
			originalString = String.valueOf(BooleanUtils.toInteger((Boolean) original));
		} else {
			originalString = String.valueOf(original);
		}

		if (targetClass == String.class)
			return originalString;

		if (StringUtils.isBlank(originalString))
			return null;

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
			else if (targetClass == Boolean.class) {
				boolean boolNumber = false;
				if (original instanceof Number)
					boolNumber = boolNumber((Number) original);
				return BooleanUtils.toBoolean(originalString) || boolNumber;
			} else if (targetClass == Byte.class)
				return Byte.valueOf(originalString);
			else if (targetClass == Character.class)
				return Character.valueOf((originalString).charAt(0));
			else if (targetClass == Date.class) {
				// 增加日期格式的兼容性
				Date date = null;
				if (date == null) {
					try {
						date = new Date(Long.valueOf(originalString));
					} catch (Exception e) {
					}
				}
				if (date == null) {
					try {
						date = DateUtils.parseDate(originalString, CoreDef.DATETIME_FORMAT);
					} catch (Exception e) {
					}
				}
				if (date == null) {
					try {
						date = DateUtils.parseDate(originalString, CoreDef.DATE_FORMAT);
					} catch (Exception e) {
					}
				}
				return date;
			} else if (targetClass.getName().equals("short"))
				return Short.valueOf(originalString);
			else if (targetClass.getName().equals("int"))
				return Integer.valueOf(originalString);
			else if (targetClass.getName().equals("long"))
				return Long.valueOf(originalString);
			else if (targetClass.getName().equals("double"))
				return Double.valueOf(originalString);
			else if (targetClass.getName().equals("float"))
				return Float.valueOf(originalString);
			else if (targetClass.getName().equals("boolean")) {
				boolean boolNumber = false;
				if (original instanceof Number)
					boolNumber = boolNumber((Number) original);
				return BooleanUtils.toBoolean(originalString) || boolNumber;
			} else if (targetClass.getName().equals("byte"))
				return Byte.valueOf(originalString);
			else if (targetClass.getName().equals("char"))
				return Character.valueOf((originalString).charAt(0));

			throw new RuntimeException("Unsupport target basic type:" + targetClass.getName());
		} catch (Exception e) {
			throw new RuntimeException("Cast failed from String [" + originalString + "] to " + targetClass.getName(), e);
		}
	}

	/**
	 * If code reaches here, the number can't be null, unless current code has
	 * been modified
	 */
	private final static boolean boolNumber(Number number) {
		if (number == null)
			return false;
		// based on R32-24
		if (number instanceof Float && number.floatValue() < 10E-6 && number.floatValue() > -10E-6)
			return false;
		// based on R64-53
		if (number.doubleValue() < 10E-15 && number.doubleValue() > -10E-15)
			return false;
		return true;
	}

	/**
	 * Collection-based simple cast, from one {@link java.util.Collection} type
	 * to Array-based basic type<br>
	 * However, original sort may be changed based on the container you
	 * selected.
	 * 
	 * @param originalArray
	 * @param targetClass
	 * @return Object of convert result
	 * @throws RuntimeException
	 *             If convert failed
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static Object arrayTypeCast(Collection originalArray, Class targetClass) {
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
					// else if (SortedSet.class.isAssignableFrom(targetClass))
					// targetArray = new TreeSet();
					else if (Set.class.isAssignableFrom(targetClass))
						targetArray = new HashSet();
					// else if
					// (TransferQueue.class.isAssignableFrom(targetClass))
					// targetArray = new LinkedTransferQueue();
					// else if
					// (BlockingQueue.class.isAssignableFrom(targetClass))
					// targetArray = new SynchronousQueue();
					else if (Deque.class.isAssignableFrom(targetClass))
						targetArray = new LinkedList();
					// If no matches
					else
						targetArray = new ArrayList();
				}
				if (targetArray == null)
					throw new RuntimeException("Unsupport target Array type:" + targetClass.getName());
				((Collection) targetArray).addAll(originalArray);
			} else
				throw new RuntimeException("Unsupport target type(" + targetClass.getName() + "), maybe it isn't Array?");
			return targetArray;
		} catch (Exception e) {
			throw new RuntimeException("Cast failed from " + originalArray.getClass().getName() + " {" + originalArray + "} to "
					+ targetClass.getName(), e);
		}
	}

	/**
	 * Array-based simple cast, from one {@link java.util.Collection} type to
	 * Array-based basic type<br>
	 * However, original sort may be changed based on the container you
	 * selected.
	 * 
	 * @param originalArray
	 * @param targetClass
	 * @return Object of convert result
	 * @throws RuntimeException
	 *             If convert failed
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static <T> Object arrayTypeCast(T[] originalArray, Class targetClass) {
		if (originalArray == null)
			throw new NullPointerException("Parameter originalArray is null");
		if (targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");

		Object targetArray = null;
		try {
			if (targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$")) {
				targetArray = Arrays.copyOf(originalArray, originalArray.length, targetClass);
			} else if (Collection.class.isAssignableFrom(targetClass)) {
				try {
					// Generic instance, if this type could be initialized
					targetArray = targetClass.newInstance();
				} catch (Exception e) {
					// If can't initialize, it maybe an interface
					if (List.class.isAssignableFrom(targetClass))
						targetArray = new ArrayList();
					// else if (SortedSet.class.isAssignableFrom(targetClass))
					// targetArray = new TreeSet();
					else if (Set.class.isAssignableFrom(targetClass))
						targetArray = new HashSet();
					// else if
					// (TransferQueue.class.isAssignableFrom(targetClass))
					// targetArray = new LinkedTransferQueue();
					// else if
					// (BlockingQueue.class.isAssignableFrom(targetClass))
					// targetArray = new SynchronousQueue();
					else if (Deque.class.isAssignableFrom(targetClass))
						targetArray = new LinkedList();
					// If no matches
					else
						targetArray = new ArrayList();
				}
				if (targetArray == null)
					throw new RuntimeException("Unsupport target Array type:" + targetClass.getName());
				((Collection) targetArray).addAll(Arrays.asList(originalArray));
			} else
				throw new RuntimeException("Unsupport target type(" + targetClass.getName() + "), maybe it isn't Array?");
			return targetArray;
		} catch (Exception e) {
			throw new RuntimeException("Cast failed from " + originalArray.getClass().getName() + " {" + originalArray + "} to "
					+ targetClass.getName(), e);
		}
	}

}