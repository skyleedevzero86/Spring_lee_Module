package com.sleekydz86.productrecommend.global.config;

import com.sleekydz86.productrecommend.adapter.in.mcp.ProductSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiToolConfiguration {

	@Bean
	ToolCallbackProvider productSearchToolCallbacks(ProductSearchTool productSearchTool) {
		return MethodToolCallbackProvider.builder()
			.toolObjects(productSearchTool)
			.build();
	}
}
