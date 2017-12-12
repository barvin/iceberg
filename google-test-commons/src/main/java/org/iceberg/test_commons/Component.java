package org.iceberg.test_commons;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Component {
    public static final String SEARCH = "Search";

    private Component() {
    }

    public static boolean isComponent(String value) {
        List<Field> constants = Arrays.stream(Component.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()))
                .collect(Collectors.toList());
        return constants.stream().anyMatch(constant -> {
            try {
                return constant.get(null).equals(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
