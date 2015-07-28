package net.vicp.lylab.utils;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exception.LYException;

public final class Caster extends NonCloneableBaseObject {

	public static Object simpleCast(Object originalObject, Class<?> targetClass) {
		if(originalObject == null)
			throw new NullPointerException("Parameter originalObject is null");
		if(targetClass == null)
			throw new NullPointerException("Parameter targetClass is null");
		if(targetClass == originalObject.getClass())
			return originalObject;

		Object result = null;
		if(originalObject.getClass() == String.class) {
			try {
				if(targetClass == String.class)
					result = new String((String) originalObject);
				else if(targetClass == Short.class)
					result = Short.valueOf((String) originalObject);
				else if(targetClass == Integer.class)
					result = Integer.valueOf((String) originalObject);
				else if(targetClass == Long.class)
					result = Long.valueOf((String) originalObject);
				else if(targetClass == Double.class)
					result = Double.valueOf((String) originalObject);
				else if(targetClass == Float.class)
					result = Float.valueOf((String) originalObject);
				else if(targetClass == Boolean.class)
					result = Boolean.valueOf((String) originalObject);
				else if(targetClass == Byte.class)
					result = Byte.valueOf((String) originalObject);
				else if(targetClass == Character.class)
					result = Character.valueOf(((String) originalObject).charAt(0));
				else if(targetClass.getName() == "short")
					result = Short.valueOf((String) originalObject);
				else if(targetClass.getName() == "int")
					result = Integer.valueOf((String) originalObject);
				else if(targetClass.getName() == "long")
					result = Long.valueOf((String) originalObject);
				else if(targetClass.getName() == "double")
					result = Double.valueOf((String) originalObject);
				else if(targetClass.getName() == "float")
					result = Float.valueOf((String) originalObject);
				else if(targetClass.getName() == "boolean")
					result = Boolean.valueOf((String) originalObject);
				else if(targetClass.getName() == "byte")
					result = Byte.valueOf((String) originalObject);
				else if(targetClass.getName() == "char")
					result = Character.valueOf(((String) originalObject).charAt(0));
				else
					throw new LYException("Unsupport target type:" + targetClass.getName());
			} catch (Exception e) {
				throw new LYException("Cast failed from String [" + originalObject + "] to " + targetClass.getName(), e);
			}
		} else if(targetClass == String.class) {
			result = originalObject.toString();
		} else
			throw new LYException("Unsupport target type:" + targetClass.getName());
		return result;
	}

}
