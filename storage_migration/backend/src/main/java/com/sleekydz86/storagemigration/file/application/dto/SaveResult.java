package com.sleekydz86.storagemigration.file.application.dto;

public record SaveResult(StoredFileResult file, long storedBytes, long databaseBytes, String checksum,
                         boolean verified) {
}
