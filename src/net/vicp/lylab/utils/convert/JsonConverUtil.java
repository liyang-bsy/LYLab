package net.vicp.lylab.utils.convert;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;

public class JsonConverUtil extends NonCloneableBaseObject {
	
	private static final JsonFactory jsonFactory = new JsonFactory();
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		class JsonDateDeserializer extends JsonDeserializer<Date> {
		    @Override
		    public Date deserialize(JsonParser parser, DeserializationContext deserializationContext)
		        throws IOException, JsonProcessingException {
		        String dateText = parser.getText();
		        SimpleDateFormat dateFormat = new SimpleDateFormat(CoreDef.DATETIME_FORMAT);
		        try {
		            return dateFormat.parse(dateText);
		        } catch (ParseException e) {
		            throw new RuntimeException("Can't parse date " + dateText, e);
		        }
		    }
		}
		class JsonDateSerializer extends JsonSerializer<Date> {
			@Override
			public void serialize(Date date, JsonGenerator gen, SerializerProvider provider)
					throws IOException, JsonProcessingException {
				SimpleDateFormat dateFormat = new SimpleDateFormat(CoreDef.DATETIME_FORMAT);
				String formattedDate = dateFormat.format(date);
				gen.writeString(formattedDate);
				provider.getConfig().getDateFormat();
			}
		}

		getMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).setDateFormat(new SimpleDateFormat(CoreDef.DATETIME_FORMAT));
		
        SimpleModule sweModule = new SimpleModule("DateTimeFormatModule");
        sweModule.addDeserializer(Date.class, new JsonDateDeserializer());
        sweModule.addSerializer(Date.class, new JsonDateSerializer());
        getMapper().registerModule(sweModule);
        
	}
	
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

	public static JsonFactory getJsonfactory() {
		return jsonFactory;
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

}
