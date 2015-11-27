package net.vicp.lylab.utils;

import java.util.*;
import java.util.concurrent.*;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public final class Caster extends NonCloneableBaseObject {public static void main(String[] args) throws InstantiationException, IllegalAccessException {
	Class<?>[] classes = new Class<?>[] {
		java.util.AbstractCollection.class	,
		java.util.AbstractList.class	,
		java.util.AbstractMap.class	,
		java.util.AbstractQueue.class	,
		java.util.AbstractSequentialList.class	,
		java.util.AbstractSet.class	,
		java.util.ArrayDeque.class	,
		java.util.ArrayList.class	,
		java.util.Arrays.class	,
		java.util.BitSet.class	,
		java.util.Calendar.class	,
		java.util.Collection.class	,
		java.util.Collections.class	,
//		java.util.ComparableTimSort.class	,
		java.util.Comparator.class	,
		java.util.ConcurrentModificationException.class	,
		java.util.Currency.class	,
		java.util.Date.class	,
		java.util.Deque.class	,
		java.util.Dictionary.class	,
//		java.util.DualPivotQuicksort.class	,
		java.util.DuplicateFormatFlagsException.class	,
		java.util.EmptyStackException.class	,
		java.util.Enumeration.class	,
		java.util.EnumMap.class	,
		java.util.EnumSet.class	,
		java.util.EventListener.class	,
		java.util.EventListenerProxy.class	,
		java.util.EventObject.class	,
		java.util.FormatFlagsConversionMismatchException.class	,
		java.util.Formattable.class	,
		java.util.FormattableFlags.class	,
		java.util.Formatter.class	,
		java.util.FormatterClosedException.class	,
		java.util.GregorianCalendar.class	,
		java.util.HashMap.class	,
		java.util.HashSet.class	,
		java.util.Hashtable.class	,
		java.util.IdentityHashMap.class	,
		java.util.IllegalFormatCodePointException.class	,
		java.util.IllegalFormatConversionException.class	,
		java.util.IllegalFormatException.class	,
		java.util.IllegalFormatFlagsException.class	,
		java.util.IllegalFormatPrecisionException.class	,
		java.util.IllegalFormatWidthException.class	,
		java.util.IllformedLocaleException.class	,
		java.util.InputMismatchException.class	,
		java.util.InvalidPropertiesFormatException.class	,
		java.util.Iterator.class	,
//		java.util.JapaneseImperialCalendar.class	,
//		java.util.JumboEnumSet.class	,
		java.util.LinkedHashMap.class	,
		java.util.LinkedHashSet.class	,
		java.util.LinkedList.class	,
		java.util.List.class	,
		java.util.ListIterator.class	,
		java.util.ListResourceBundle.class	,
		java.util.Locale.class	,
//		java.util.LocaleISOData.class	,
		java.util.Map.class	,
		java.util.MissingFormatArgumentException.class	,
		java.util.MissingFormatWidthException.class	,
		java.util.MissingResourceException.class	,
		java.util.NavigableMap.class	,
		java.util.NavigableSet.class	,
		java.util.NoSuchElementException.class	,
		java.util.Objects.class	,
		java.util.Observable.class	,
		java.util.Observer.class	,
		java.util.PriorityQueue.class	,
		java.util.Properties.class	,
		java.util.PropertyPermission.class	,
//		java.util.PropertyPermissionCollection.class	,
		java.util.PropertyResourceBundle.class	,
		java.util.Queue.class	,
		java.util.Random.class	,
		java.util.RandomAccess.class	,
//		java.util.RandomAccessSubList.class	,
//		java.util.RegularEnumSet.class	,
		java.util.ResourceBundle.class	,
		java.util.Scanner.class	,
		java.util.ServiceConfigurationError.class	,
		java.util.ServiceLoader.class	,
		java.util.Set.class	,
		java.util.SimpleTimeZone.class	,
		java.util.SortedMap.class	,
		java.util.SortedSet.class	,
		java.util.Stack.class	,
		java.util.StringTokenizer.class	,
//		java.util.SubList.class	,
//		java.util.TaskQueue.class	,
		java.util.Timer.class	,
		java.util.TimerTask.class	,
//		java.util.TimerThread.class	,
		java.util.TimeZone.class	,
//		java.util.TimSort.class	,
		java.util.TooManyListenersException.class	,
		java.util.TreeMap.class	,
		java.util.TreeSet.class	,
		java.util.UnknownFormatConversionException.class	,
		java.util.UnknownFormatFlagsException.class	,
		java.util.UUID.class	,
		java.util.Vector.class	,
		java.util.WeakHashMap.class	,
//		java.util.XMLUtils.class	,


	};
	for (Class<?> targetClass : classes) {
		try {
			if(targetClass.isInterface())
			{
				System.out.println(targetClass.getName() + "\tis interface");
				continue;
			}
				
			targetClass.newInstance();
			System.out.println(targetClass.getName() + "\tcan");
		} catch (InstantiationException e) {
//			System.out.println(targetClass.getName() + "\tcan't initialize");
		} catch (IllegalAccessException e) {
//			System.out.println(targetClass.getName() + "\tcan't access");
		}
	}
}

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
			else if(targetClass.getName().equals("short"))
				return Short.valueOf(originalString);
			else if(targetClass.getName().equals("int"))
				return Integer.valueOf(originalString);
			else if(targetClass.getName().equals("long"))
				return Long.valueOf(originalString);
			else if(targetClass.getName().equals("double"))
				return Double.valueOf(originalString);
			else if(targetClass.getName().equals("float"))
				return Float.valueOf(originalString);
			else if(targetClass.getName().equals("boolean"))
				return Boolean.valueOf(originalString);
			else if(targetClass.getName().equals("byte"))
				return Byte.valueOf(originalString);
			else if(targetClass.getName().equals("char"))
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
			if(targetClass.getName().matches("^\\[L[a-zA-Z0-9_.]*;$")) {
				targetArray = Arrays.copyOf(originalArray.toArray(), originalArray.size(), targetClass);
			}
			else if(Collection.class.isAssignableFrom(targetClass)) {
				try {
					// Generic instance, if this type could be initialized
					targetArray = targetClass.newInstance();
				} catch (InstantiationException e) {
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
				((Collection) targetArray).addAll(originalArray);
			}
			if(targetArray == null)
				throw new LYException("Unsupport target type:" + targetClass.getName());
			return targetArray;
		} catch (Exception e) {
			throw new LYException("Cast failed from ArrayList [" + originalArray + "] to " + targetClass.getName(), e);
		}
	}

}
