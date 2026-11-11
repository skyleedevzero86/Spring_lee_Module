package com.sleekydz86.storagemigration.global.util;

import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
