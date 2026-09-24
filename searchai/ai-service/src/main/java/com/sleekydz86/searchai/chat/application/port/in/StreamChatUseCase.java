package com.sleekydz86.searchai.chat.application.port.in;

import com.sleekydz86.searchai.chat.domain.ChatQuery;
import com.sleekydz86.searchai.chat.domain.StreamChunk;
import reactor.core.publisher.Flux;

public interface StreamChatUseCase {

	Flux<StreamChunk> streamChat(ChatQuery query);
}
