package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.ChangeProductPriceCommand;
import com.sleekydz86.catalogflow.application.command.CreateProductCommand;
import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.ProductLifecycleCommand;
import com.sleekydz86.catalogflow.application.command.UpdateProductCommand;

public interface CreateProductUseCase {

	ProductCommandResult create(CreateProductCommand command);
}
