package com.sleekydz86.searchai.service;

import com.sleekydz86.searchai.global.beans.SearchResult;
import java.util.List;

public interface SearXngService {
    List<SearchResult> search(String query);
}