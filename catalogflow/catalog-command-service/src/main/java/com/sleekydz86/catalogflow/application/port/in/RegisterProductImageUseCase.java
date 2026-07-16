package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.RegisterProductImageCommand;

public interface RegisterProductImageUseCase {

	ProductCommandResult registerImage(RegisterProductImageCommand command);
}
