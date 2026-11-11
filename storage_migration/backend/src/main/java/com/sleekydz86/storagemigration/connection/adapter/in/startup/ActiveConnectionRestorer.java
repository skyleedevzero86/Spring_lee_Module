package com.sleekydz86.storagemigration.connection.adapter.in.startup;

import com.sleekydz86.storagemigration.connection.application.port.in.ActivateDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.DbConnectionQueryUseCase;
import com.sleekydz86.storagemigration.global.web.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ActiveConnectionRestorer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ActiveConnectionRestorer.class);

    private final DbConnectionQueryUseCase query;
    private final ActivateDbConnectionUseCase activate;

    public ActiveConnectionRestorer(DbConnectionQueryUseCase query, ActivateDbConnectionUseCase activate) {
        this.query = query;
        this.activate = activate;
    }

    @Override
    public void run(ApplicationArguments args) {
        query.active().ifPresent(connection -> {
            try {
                activate.activate(connection.id(), Actor.system());
                log.info("사용 중이던 DB 연결을 다시 열었습니다: {}", connection.name());
            } catch (RuntimeException exception) {
                log.warn("사용 중이던 DB 연결을 다시 열지 못했습니다: {} ({})", connection.name(), exception.getMessage());
            }
        });
    }
}
