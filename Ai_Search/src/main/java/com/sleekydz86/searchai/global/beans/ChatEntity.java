package com.sleekydz86.searchai.global.beans;

import com.sleekydz86.searchai.global.enums.ChatMode;
import lombok.Data;

@Data
public class ChatEntity {
    private String currentUserName;
    private String message;
    private String botMsgId;
    private ChatMode mode;

}