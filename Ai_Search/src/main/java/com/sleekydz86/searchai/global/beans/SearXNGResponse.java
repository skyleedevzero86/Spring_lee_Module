package com.sleekydz86.searchai.global.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SearXNGResponse {

    private String query;

    private List<SearchResult> results;

}