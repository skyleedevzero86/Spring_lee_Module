package com.sleekydz86.storagemigration.global.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(BlobSource.class)
public class BlobSourceTypeHandler extends BaseTypeHandler<BlobSource> {

    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, BlobSource source, JdbcType jdbcType)
            throws SQLException {
        String product = statement.getConnection().getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        if (product.contains("postgres")) {
            statement.setBinaryStream(index, source.stream(), source.length());
        } else {
            statement.setBlob(index, source.stream(), source.length());
        }
    }

    @Override
    public BlobSource getNullableResult(ResultSet resultSet, String columnName) {
        throw new UnsupportedOperationException("BlobSource는 입력 전용입니다.");
    }

    @Override
    public BlobSource getNullableResult(ResultSet resultSet, int columnIndex) {
        throw new UnsupportedOperationException("BlobSource는 입력 전용입니다.");
    }

    @Override
    public BlobSource getNullableResult(CallableStatement statement, int columnIndex) {
        throw new UnsupportedOperationException("BlobSource는 입력 전용입니다.");
    }
}
