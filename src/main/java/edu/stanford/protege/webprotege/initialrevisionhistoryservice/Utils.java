package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import java.util.Collection;

public class Utils {

    public static <T> boolean isNotEquals(T a, T b) {
        if (a == null && b == null) {
            return false;
        } else if (a == null) {
            return true;
        } else if (b == null) {
            return true;
        }
        return !a.equals(b);
    }

    public static <T extends Collection> boolean isNotEmpty(T object) {
        return !object.isEmpty();
    }
}
