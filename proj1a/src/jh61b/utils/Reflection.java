package jh61b.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public class Reflection {
    public static Stream<Field> getFields(Class<?> c) {
        return Arrays.stream(c.getDeclaredFields());
    }
}
