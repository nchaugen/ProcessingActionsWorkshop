package integration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Json {

    private Json() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String write(Object object) {
        Objects.requireNonNull(object, "object");
        try {
            // Jackson workaround: Using List/Map as an intermediate value will exclude null fields, as intended
            if (object instanceof Collection<?> || object instanceof Map) {
                Class<?> type = object instanceof Collection<?> ? List.class : Map.class;
                return MAPPER.writeValueAsString(MAPPER.convertValue(object, type));
            }
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "This method only works for JSON objects and arrays. " +
                    "Failed to write " + object + " (of " + object.getClass() + ")", e);
        }
    }

    public static <T> T read(Class<T> type, String json) {
        try {
            return MAPPER.readerFor(type).readValue(Objects.requireNonNull(json, "jsonString"));
        } catch (Exception e) {
            return summaryFailure(json, e);
        }
    }

    private static <T> T summaryFailure(String json, Exception e) {
        String summary = json.length() > 32
            ? json.substring(0, 29) + "..."
            : json;
        throw new IllegalArgumentException("Failed to read [" + json.length() + " chars]: " + summary, e);
    }
}
