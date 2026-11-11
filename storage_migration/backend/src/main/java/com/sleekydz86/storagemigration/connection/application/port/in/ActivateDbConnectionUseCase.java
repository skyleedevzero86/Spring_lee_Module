package com.sleekydz86.storagemigration.connection.application.port.in;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import com.sleekydz86.storagemigration.global.web.Actor;

public interface ActivateDbConnectionUseCase {

    DbConnectionResult activate(String id, Actor actor);
}
