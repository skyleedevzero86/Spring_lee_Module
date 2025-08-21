package com.sleekydz86.searchai.global.beans;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    private String id;
    private String title;
    private String text;
    private String url;
    private String metadata;
    private Double score;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public String getText() {
        return this.text;
    }

    public Document(String text) {
        this.text = text;
        this.createdAt = java.time.LocalDateTime.now();
    }

    public Document(String title, String text) {
        this.title = title;
        this.text = text;
        this.createdAt = java.time.LocalDateTime.now();
    }
}