package com.sleekydz86.searchai.gateway.global.config;

import com.sleekydz86.searchai.proto.AiChatServiceGrpc;
import com.sleekydz86.searchai.proto.KnowledgeServiceGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfiguration {

	@Bean
	AiChatServiceGrpc.AiChatServiceStub aiChatAsyncStub(GrpcChannelFactory channels) {
		ManagedChannel channel = channels.createChannel("ai-service");
		return AiChatServiceGrpc.newStub(channel);
	}

	@Bean
	KnowledgeServiceGrpc.KnowledgeServiceBlockingStub knowledgeBlockingStub(GrpcChannelFactory channels) {
		ManagedChannel channel = channels.createChannel("ai-service");
		return KnowledgeServiceGrpc.newBlockingStub(channel);
	}

	@Bean
	com.sleekydz86.searchai.proto.UsageServiceGrpc.UsageServiceBlockingStub usageBlockingStub(GrpcChannelFactory channels) {
		ManagedChannel channel = channels.createChannel("ai-service");
		return com.sleekydz86.searchai.proto.UsageServiceGrpc.newBlockingStub(channel);
	}
}
