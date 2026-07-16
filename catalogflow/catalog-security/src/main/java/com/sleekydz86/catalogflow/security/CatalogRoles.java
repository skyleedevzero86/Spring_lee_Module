package com.sleekydz86.catalogflow.security;

public final class CatalogRoles {

	public static final String VIEWER = "CATALOG_VIEWER";
	public static final String EDITOR = "CATALOG_EDITOR";
	public static final String MANAGER = "CATALOG_MANAGER";
	public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

	public static final String ROLE_VIEWER = "ROLE_" + VIEWER;
	public static final String ROLE_EDITOR = "ROLE_" + EDITOR;
	public static final String ROLE_MANAGER = "ROLE_" + MANAGER;
	public static final String ROLE_SYSTEM_ADMIN = "ROLE_" + SYSTEM_ADMIN;

	private CatalogRoles() {
	}
}
