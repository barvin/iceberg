package org.home.reportportal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RpUtils {
    public static Set<String> parseAsSet(String rawTags) {
        if (rawTags == null) {
            return null;
        }
        Set<String> result = new HashSet<>();
        Collections.addAll(result, rawTags.trim().split(";"));
        return result;
    }

    public static String getStackTraceString(Throwable e) {
        StringBuilder result = new StringBuilder();

        for (StackTraceElement stElement : e.getStackTrace()) {
            result.append(stElement).append(System.getProperty("line.separator"));
        }

        return result.toString();
    }
}
