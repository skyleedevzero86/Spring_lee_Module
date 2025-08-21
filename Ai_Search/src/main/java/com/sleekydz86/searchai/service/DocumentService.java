package com.sleekydz86.searchai.service;

import com.sleekydz86.searchai.global.beans.Document;

import java.util.List;

public interface  DocumentService {
    List<Document> doSearch(String question);
}
