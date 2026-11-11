package com.sleekydz86.storagemigration.global.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class ActorArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String WORKER_HEADER = "X-Worker";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Actor.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String worker = webRequest.getHeader(WORKER_HEADER);
        String decoded = worker == null ? null : URLDecoder.decode(worker, StandardCharsets.UTF_8);
        return new Actor(decoded, clientIp(request));
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return normalize(forwarded.split(",")[0].trim());
        }
        return normalize(request.getRemoteAddr());
    }

    private String normalize(String address) {
        if ("0:0:0:0:0:0:0:1".equals(address) || "::1".equals(address)) {
            return "127.0.0.1";
        }
        return address;
    }
}
