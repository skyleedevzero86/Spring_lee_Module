package com.sleekydz86.searchai.service;

import com.sleekydz86.searchai.global.beans.ChatEntity;

public interface ChatService {
    void streamChat(ChatEntity chatEntity);
}
