package com.lin1987www.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;

public class JacksonHelper {
	private static JsonFactory mJsonFactory = null;

	public static JsonFactory getJsonFactory() {
		if (mJsonFactory == null) {
			mJsonFactory = new JsonFactory();
		}
		return mJsonFactory;
	}

	private static ObjectMapper mObjectMapper = null;

	public static ObjectMapper getObjectMapper() {
		if (mObjectMapper == null) {
			mObjectMapper = new ObjectMapper();
			mObjectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return mObjectMapper;
	}

	protected final static TypeFactory mTypeFactory = getObjectMapper()
			.getTypeFactory();

	/**
	 * 
	 * JacksonHelper.<String> Parse("123456") => "123456"
	 * JacksonHelper.<Integer> Parse("123456") => 123456
	 * 
	 * @param text
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static <T> T Parse(String text) throws JsonParseException,
			IOException {
		return JacksonHelper.<T> Parse(text, new TypeReference<T>() {
		});
	}

	/**
	 * 
	 * JacksonHelper.<String> Parse("123456",new TypeReference<String>(){})
	 * "123456"
	 * 
	 * JacksonHelper.<Integer> Parse("123456",new TypeReference<Integer>(){})
	 * 123456
	 * 
	 * @param text
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static <T> T Parse(String text, TypeReference<?> typeReference)
			throws JsonParseException, IOException {
		T result;
		JsonParser jsonParser = getJsonFactory().createParser(text);
		result = getObjectMapper().readValue(jsonParser, typeReference);
		return result;
	}

	/**
	 * JacksonHelper.<HashMap<String,String>>
	 * Parse("{'id':'1', 'name':'john'}",JacksonHelper
	 * .GenericType(HashMap.class, String.class, String.class))
	 * 
	 * @param text
	 * @param pJavaType
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public static <T> T Parse(String text, JavaType pJavaType)
			throws JsonParseException, IOException {
		T result;
		JsonParser jsonParser = getJsonFactory().createParser(text);
		result = getObjectMapper().readValue(jsonParser, pJavaType);
		return result;
	}

	/**
	 * Example:
	 * 
	 * List<String>
	 * 
	 * GenericType(List.class, String.class)
	 * 
	 * ---------------
	 * 
	 * HashMap<String,String>
	 * 
	 * GenericType(HashMap.class, String.class, String.class)
	 * 
	 * ---------------
	 * 
	 * HashMap<String,List<String>>
	 * 
	 * GenericType(HashMap.class, String.class, GenericType(List.class,
	 * String.class))
	 * 
	 * @param containerClass
	 * @param contentClass
	 * @return
	 */
	public static JavaType GenericType(Class<?> containerClass,
			JavaType contentClass) {
		return mTypeFactory.constructParametricType(containerClass,
				contentClass);
	}

	/**
	 * Example:
	 * 
	 * List<String>
	 * 
	 * GenericType(List.class, String.class)
	 * 
	 * ---------------
	 * 
	 * HashMap<String,String>
	 * 
	 * GenericType(HashMap.class, String.class, String.class)
	 * 
	 * ---------------
	 * 
	 * HashMap<String,List<String>>
	 * 
	 * GenericType(HashMap.class, String.class, GenericType(List.class,
	 * String.class))
	 * 
	 * @param containerClass
	 * @param contentClass
	 * @return
	 */
	public static JavaType GenericType(Class<?> containerClass,
			Class<?>... contentClass) {
		return mTypeFactory.constructParametricType(containerClass,
				contentClass);
	}

	public static String toJson(Object pObject) {
		String json = "{}";
		try {
			json = getObjectMapper().writeValueAsString(pObject);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}
}
