package com.sleekydz86.storagemigration.global.jdbc;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.mybatis.MyBatisFactory;
import com.sleekydz86.storagemigration.global.util.Throwables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveDatabase implements DisposableBean {

    private final AtomicReference<Session> session = new AtomicReference<>();
    private final SchemaInitializer schemaInitializer;

    public ActiveDatabase(SchemaInitializer schemaInitializer) {
        this.schemaInitializer = schemaInitializer;
    }

    public void switchTo(String connectionId, SqlDialect dialect, String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("storage-migration-" + dialect.name().toLowerCase());
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(5000);
        HikariDataSource dataSource;
        try {
            dataSource = new HikariDataSource(config);
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.DB_CONNECTION_FAILED, Throwables.rootMessage(exception), exception);
        }
        SqlSessionFactory sessionFactory;
        try {
            schemaInitializer.ensureSchema(new JdbcTemplate(dataSource), dialect);
            sessionFactory = MyBatisFactory.build(dataSource, dialect);
        } catch (RuntimeException exception) {
            dataSource.close();
            throw new BusinessException(ErrorCode.DB_OPERATION_FAILED,
                    "테이블·프로시저를 준비하지 못했습니다. " + Throwables.rootMessage(exception), exception);
        }
        Session previous = session.getAndSet(new Session(connectionId, dialect, dataSource, sessionFactory));
        if (previous != null) {
            previous.dataSource().close();
        }
    }

    public SqlSessionFactory sqlSessionFactory() {
        return current().sessionFactory();
    }

    public SqlDialect dialect() {
        return current().dialect();
    }

    public Optional<String> connectionId() {
        return Optional.ofNullable(session.get()).map(Session::connectionId);
    }

    public boolean isConnected() {
        return session.get() != null;
    }

    public void disconnect() {
        Session previous = session.getAndSet(null);
        if (previous != null) {
            previous.dataSource().close();
        }
    }

    @Override
    public void destroy() {
        disconnect();
    }

    private Session current() {
        Session current = session.get();
        if (current == null) {
            throw new BusinessException(ErrorCode.DB_NOT_CONNECTED);
        }
        return current;
    }

    private record Session(String connectionId, SqlDialect dialect, HikariDataSource dataSource,
                           SqlSessionFactory sessionFactory) {
    }
}
