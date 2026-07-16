package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.CreatePresignedUploadUrlCommand;
import com.sleekydz86.catalogflow.application.command.PresignedUploadUrlResult;

public interface CreatePresignedUploadUrlUseCase {

	PresignedUploadUrlResult create(CreatePresignedUploadUrlCommand command);
}
