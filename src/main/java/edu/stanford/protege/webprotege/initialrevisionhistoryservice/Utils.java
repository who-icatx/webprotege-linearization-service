package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import java.util.Collection;

public class Utils {

    public static <T> boolean isNotEquals(T a, T b) {
        return !a.equals(b);
    }

    public static <T extends Collection> boolean isNotEmpty(T object){
        return !object.isEmpty();
    }
}
