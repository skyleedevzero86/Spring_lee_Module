package com.sleekydz86.storagemigration.global.mybatis;

import com.sleekydz86.storagemigration.global.jdbc.SqlDialect;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import javax.sql.DataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public final class MyBatisFactory {

    public static final String MAPPER_LOCATION = "classpath*:mapper/*.xml";

    private MyBatisFactory() {
    }

    public static SqlSessionFactory build(DataSource dataSource, SqlDialect dialect) {
        Configuration configuration = new Configuration(
                new Environment("active", new JdbcTransactionFactory(), dataSource));
        configuration.setDatabaseId(dialect.name().toLowerCase(Locale.ROOT));
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        try {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION)) {
                try (InputStream input = resource.getInputStream()) {
                    new XMLMapperBuilder(input, configuration, resource.getURL().toString(),
                            configuration.getSqlFragments()).parse();
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
