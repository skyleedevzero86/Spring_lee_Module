package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.command.ChangeProductPriceCommand;
import com.sleekydz86.catalogflow.application.command.ProductCommandResult;

public interface ChangeProductPriceUseCase {

	ProductCommandResult changePrice(ChangeProductPriceCommand command);
}
