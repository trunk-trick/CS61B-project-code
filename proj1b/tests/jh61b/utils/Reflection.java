package jh61b.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Utility class for reflection-based testing.
 */
public class Reflection {

    /**
     * Returns a stream of all declared fields from the given class.
     *
     * @param clazz the class to inspect
     * @return a stream of declared fields
     */
    public static Stream<Field> getFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields());
    }
}
