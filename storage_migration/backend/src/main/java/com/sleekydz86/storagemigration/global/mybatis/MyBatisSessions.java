package com.sleekydz86.storagemigration.global.mybatis;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.jdbc.ActiveDatabase;
import com.sleekydz86.storagemigration.global.util.Throwables;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Component;

@Component
public class MyBatisSessions {

    private final ActiveDatabase activeDatabase;

    public MyBatisSessions(ActiveDatabase activeDatabase) {
        this.activeDatabase = activeDatabase;
    }

    public boolean connected() {
        return activeDatabase.isConnected();
    }

    public <M, R> R execute(Class<M> mapperType, Function<M, R> work) {
        try (SqlSession session = activeDatabase.sqlSessionFactory().openSession(true)) {
            return work.apply(session.getMapper(mapperType));
        } catch (PersistenceException exception) {
            throw new BusinessException(ErrorCode.DB_OPERATION_FAILED, Throwables.rootMessage(exception), exception);
        }
    }

    public <M, P extends ProcResult> void call(Class<M> mapperType, P params, BiConsumer<M, P> procedure,
                                               String procedureName) {
        execute(mapperType, mapper -> {
            procedure.accept(mapper, params);
            return null;
        });
        params.verify(procedureName);
    }
}
