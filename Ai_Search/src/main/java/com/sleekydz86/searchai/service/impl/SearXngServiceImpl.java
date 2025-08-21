package com.sleekydz86.searchai.service.impl;

import cn.hutool.json.JSONUtil;
import com.sleekydz86.searchai.global.beans.SearXNGResponse;
import com.sleekydz86.searchai.global.beans.SearchResult;
import com.sleekydz86.searchai.service.SearXngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 불러오기
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody; // ResponseBody 불러오기
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Slf4j 어노테이션 추가
public class SearXngServiceImpl implements SearXngService {

    @Value("${internet.websearch.url}")
    private String SEARXNG_URL;

    @Value("${internet.websearch.counts}")
    private Integer SEARXNG_COUNTS;

    private final OkHttpClient okHttpClient;

    @Override
    public List<SearchResult> search(String query) {
        HttpUrl url = HttpUrl.get(SEARXNG_URL)
                .newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        log.info("SearXNG에 요청을 보내는 중: {}", url);

        try (Response response = okHttpClient.newCall(request).execute()) {
            // --- 핵심 수정: 상세한 오류 정보 제공 ---
            if (!response.isSuccessful()) {
                String errorBody = "응답 본문을 가져올 수 없습니다";
                try (ResponseBody body = response.body()) {
                    if (body != null) {
                        errorBody = body.string();
                    }
                } catch (IOException e) {
                    log.error("SearXNG 오류 응답 본문 읽기 실패", e);
                }
                // 상태 코드와 응답 본문이 포함된 상세한 예외 발생
                throw new RuntimeException(String.format(
                        "SearXNG 요청 실패. 상태 코드: %d, URL: %s, 응답 본문: %s",
                        response.code(), url, errorBody
                ));
            }

            ResponseBody body = response.body();
            if (body != null) {
                String responseBody = body.string();
                // 디버깅을 위해 반환된 JSON 내용을 로그에 추가
                log.debug("SearXNG 응답 내용: {}", responseBody);

                SearXNGResponse searXNGResponse = JSONUtil.toBean(responseBody, SearXNGResponse.class);
                if (searXNGResponse != null && searXNGResponse.getResults() != null) {
                    return dealResult(searXNGResponse.getResults());
                } else {
                    log.warn("SearXNG에서 반환된 JSON을 파싱할 수 없거나 결과가 비어 있습니다. 응답: {}", responseBody);
                    return Collections.emptyList();
                }
            }
        } catch (IOException e) {
            // 네트워크 연결과 관련된 IO 예외에 대해서도 더 상세한 로그 제공
            log.error("SearXNG 요청 중 네트워크 IO 예외 발생, URL: {}", url, e);
            throw new RuntimeException("SearXNG 요청 중 네트워크 IO 예외 발생", e);
        }
        return Collections.emptyList();
    }

    private List<SearchResult> dealResult(List<SearchResult> results) {
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        // 참고: 기존의 subList와 parallelStream 조합은 results 수가 SEARXNG_COUNTS보다 작을 경우 문제가 될 수 있습니다.
        // 먼저 limit를 적용한 후 정렬하는 것이 더 안전하고 효율적입니다.
        return results.stream()
                .limit(SEARXNG_COUNTS)
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }
}