package com.sleekydz86.ftpserver.domain.ftp;

import lombok.Getter;
import java.util.Objects;
import java.util.UUID;

@Getter
public class FtpTransferId {
    private final String value;

    private FtpTransferId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FTP Transfer ID cannot be null or blank");
        }
        this.value = value;
    }

    public static FtpTransferId generate() {
        return new FtpTransferId(UUID.randomUUID().toString());
    }

    public static FtpTransferId of(String value) {
        return new FtpTransferId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtpTransferId that = (FtpTransferId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
