package org.qatools.reportportal;

import org.apache.commons.lang3.StringUtils;

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

    public static String getFullException(Throwable t) {
        String className = t.getClass().getSimpleName();
        String msg = t.getMessage() + System.lineSeparator();
        StringUtils.prependIfMissing(msg, ": ");
        String stackTrace = StringUtils.join(t.getStackTrace(), System.lineSeparator()) + System.lineSeparator();
        return className + msg + stackTrace;
    }
}
