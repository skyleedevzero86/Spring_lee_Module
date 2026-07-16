package com.sleekydz86.catalogflow.security;

public final class CatalogScopes {

	public static final String READ = "catalog.read";
	public static final String WRITE = "catalog.write";
	public static final String PUBLISH = "catalog.publish";
	public static final String ADMIN = "catalog.admin";
	public static final String BATCH_EXECUTE = "batch.execute";

	public static final String SCOPE_READ = "SCOPE_" + READ;
	public static final String SCOPE_WRITE = "SCOPE_" + WRITE;
	public static final String SCOPE_PUBLISH = "SCOPE_" + PUBLISH;
	public static final String SCOPE_ADMIN = "SCOPE_" + ADMIN;
	public static final String SCOPE_BATCH_EXECUTE = "SCOPE_" + BATCH_EXECUTE;

	private CatalogScopes() {
	}
}
