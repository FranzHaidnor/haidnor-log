package haidnor.log.common.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import java.io.IOException;

public class Jackson {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    /**
     * Object > json
     */
    public static String toJsonStr(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException("the bean parameter can not be null");
        }
        try {
            return objectMapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Object > byte[]
     */
    public static byte[] toJsonBytes(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException("the bean parameter can not be null");
        }
        try {
            return objectMapper.writeValueAsBytes(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------

    /**
     * byte[] > Object
     */
    public static <T> T toBean(byte[] jsonBytes, Class<T> type) {
        if (jsonBytes == null) {
            throw new IllegalArgumentException("the jsonBytes parameter can not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("the type parameter can not be null");
        }
        try {
            return objectMapper.readValue(jsonBytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * str > Object
     */
    public static <T> T toBean(String json, Class<T> type) {
        if (json == null) {
            throw new IllegalArgumentException("the json parameter can not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("the type parameter can not be null");
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * str > Object
     */
    public static <T> T toBean(String json, TypeReference<T> typeReference) {
        if (json == null) {
            throw new IllegalArgumentException("the json parameter can not be null");
        }
        if (typeReference == null) {
            throw new IllegalArgumentException("the typeReference parameter can not be null");
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * byte[] > Object
     */
    public static <T> T toBean(byte[] jsonBytes, TypeReference<T> typeReference) {
        if (jsonBytes == null) {
            throw new IllegalArgumentException("the jsonBytes parameter can not be null");
        }
        if (typeReference == null) {
            throw new IllegalArgumentException("the typeReference parameter can not be null");
        }
        try {
            return objectMapper.readValue(jsonBytes, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
