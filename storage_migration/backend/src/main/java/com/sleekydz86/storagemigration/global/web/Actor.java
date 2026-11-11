package com.sleekydz86.storagemigration.global.web;

public record Actor(String worker, String ip) {

    public static final String ANONYMOUS = "anonymous";

    public Actor {
        worker = worker == null || worker.isBlank() ? ANONYMOUS : worker.trim();
        ip = ip == null || ip.isBlank() ? "unknown" : ip.trim();
    }

    public static Actor system() {
        return new Actor("system", "127.0.0.1");
    }
}
