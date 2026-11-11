package com.sleekydz86.storagemigration.global.mybatis;

import java.io.InputStream;

public record BlobSource(InputStream stream, long length) {
}
