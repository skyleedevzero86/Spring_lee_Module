package com.sleekydz86.storagemigration.connection.domain;

public enum DbVendor {

    ORACLE(1521, "jdbc:oracle:thin:@//%s:%d/%s", "SELECT 1 FROM DUAL"),
    MYSQL(3306, "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC",
            "SELECT 1"),
    POSTGRESQL(5432, "jdbc:postgresql://%s:%d/%s?escapeSyntaxCallMode=callIfNoReturn", "SELECT 1");

    private final int defaultPort;
    private final String urlPattern;
    private final String validationQuery;

    DbVendor(int defaultPort, String urlPattern, String validationQuery) {
        this.defaultPort = defaultPort;
        this.urlPattern = urlPattern;
        this.validationQuery = validationQuery;
    }

    public int defaultPort() {
        return defaultPort;
    }

    public String jdbcUrl(String host, int port, String database) {
        return urlPattern.formatted(host, port, database);
    }

    public String validationQuery() {
        return validationQuery;
    }
}
