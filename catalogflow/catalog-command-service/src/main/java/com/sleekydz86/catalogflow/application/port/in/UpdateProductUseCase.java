package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.UpdateProductCommand;

public interface UpdateProductUseCase {

	ProductCommandResult update(UpdateProductCommand command);
}
