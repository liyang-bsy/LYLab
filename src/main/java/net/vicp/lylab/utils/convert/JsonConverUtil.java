package net.vicp.lylab.utils.convert;

import java.util.Objects;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

import com.alibaba.fastjson.JSON;

public class JsonConverUtil extends NonCloneableBaseObject {

	public static String serialize(Object obj) {
		Objects.requireNonNull(obj);
		try {
			String str = JSON.toJSONString(obj);
			return str;
		} catch (Exception e) {
			throw new LYException("Serialize failed", e);
		}
	}

	public static <T> T deserialize(Class<T> instanceClass, String json) {
		Objects.requireNonNull(instanceClass);
		Objects.requireNonNull(json);
		try {
			return JSON.parseObject(json, instanceClass);
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json[" + json + "] into " + instanceClass.getName(), e);
		}
	}

	public static Object deserialize(String className, String json) {
		Objects.requireNonNull(className);
		Objects.requireNonNull(json);
		try {
			return JSON.parseObject(json, Class.forName(className));
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json into " + className + "\n" + json, e);
		}
	}

}
