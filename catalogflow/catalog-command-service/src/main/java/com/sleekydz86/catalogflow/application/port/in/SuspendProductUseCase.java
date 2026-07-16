package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.ProductLifecycleCommand;

public interface SuspendProductUseCase {

	ProductCommandResult suspend(ProductLifecycleCommand command);
}
