package net.vicp.lylab.utils.convert;

import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public class JsonConverUtil extends NonCloneableBaseObject {
	
	private static final JsonFactory jsonFactory = new JsonFactory();
	private static final ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	
	public static String serialize(Object obj) {
		if (obj == null)
			throw new LYException("Parameter obj is null");
		String str = null;
		try {
			StringWriter sw = new StringWriter();
			JsonGenerator generator = jsonFactory.createGenerator(sw);
			mapper.writeValue(generator, obj);
			str = sw.toString();
			generator.close();
		} catch (Exception e) {
			throw new LYException("Serialize failed", e);
		}

		return str;
	}

	public static <T> T deserialize(Class<T> instanceClass, String json) {
		if (json == null)
			throw new LYException("Parameter json is null");
		if (instanceClass == null)
			throw new LYException("Parameter instanceClass is null");
		try {
			return mapper.readValue(json, instanceClass);
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json[" + json + "] into " + instanceClass.getName(), e);
		}
	}

	public static Object deserialize(String className, String json) {
		if (className == null)
			throw new LYException("Parameter className is null");
		try {
			return deserialize(Class.forName(className), json);
		} catch (Exception e) {
			throw new LYException("Can not deserialize follow json into " + className + "\n" + json, e);
		}
	}

}
