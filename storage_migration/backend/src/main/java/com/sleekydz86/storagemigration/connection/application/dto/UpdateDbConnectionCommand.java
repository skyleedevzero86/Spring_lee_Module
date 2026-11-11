package com.sleekydz86.storagemigration.connection.application.dto;

import com.sleekydz86.storagemigration.connection.domain.DbVendor;

public record UpdateDbConnectionCommand(String id, String name, DbVendor vendor, String host, Integer port,
                                        String database, String username, String password) {
}
