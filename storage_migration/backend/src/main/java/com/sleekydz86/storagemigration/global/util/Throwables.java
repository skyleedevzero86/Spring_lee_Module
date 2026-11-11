package com.sleekydz86.storagemigration.global.util;

public final class Throwables {

    private Throwables() {
    }

    public static String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
