package com.sleekydz86.searchai.global.beans;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String title;
    private String url;
    private String content;
    private Double score;
    private String engine;
    private String category;
    private String publishedDate;
}