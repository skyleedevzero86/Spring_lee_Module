<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="스토리지"/>
<c:set var="headerActionUrl" value="/"/>
<c:set var="headerActionLabel" value="파일 목록"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<div class="storage-summary">
    버킷 <strong><c:out value="${browse.bucketName}"/></strong> · 객체 <strong>${browse.objectCount}</strong>건
</div>

<form class="search" method="get" action="<c:url value='/storage'/>">
    <input type="text" name="keyword" value="<c:out value='${keyword}'/>" placeholder="키·파일명 검색">
    <input type="hidden" name="prefix" value="${prefix}">
    <button type="submit" class="btn">검색</button>
    <a class="btn btn-secondary" href="<c:url value='/storage'/>?prefix=${prefix}">새로고침</a>
    <c:if test="${not empty keyword}">
        <a class="btn btn-secondary" href="<c:url value='/storage'/>?prefix=${prefix}">초기화</a>
    </c:if>
</form>

<nav class="storage-tabs">
    <a class="storage-tab ${prefix == 'all' ? 'active' : ''}" href="<c:url value='/storage'/>?prefix=all<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">전체</a>
    <a class="storage-tab ${prefix == 'uploads' ? 'active' : ''}" href="<c:url value='/storage'/>?prefix=uploads<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">uploads</a>
    <a class="storage-tab ${prefix == 'thumbnails' ? 'active' : ''}" href="<c:url value='/storage'/>?prefix=thumbnails<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">thumbnails</a>
</nav>

<c:choose>
    <c:when test="${empty browse.objects}">
        <div class="empty">조건에 맞는 S3 객체가 없습니다.</div>
    </c:when>
    <c:otherwise>
        <div class="grid">
            <c:forEach var="item" items="${browse.objects}">
                <article class="card storage-card">
                    <c:choose>
                        <c:when test="${item.image and not empty item.previewUrl}">
                            <a href="<c:out value='${item.previewUrl}'/>" target="_blank" rel="noopener">
                                <img class="thumb" src="<c:out value='${item.previewUrl}'/>" alt="">
                            </a>
                        </c:when>
                        <c:otherwise>
                            <div class="thumb storage-thumb-fallback"><c:out value="${item.kindLabel}"/></div>
                        </c:otherwise>
                    </c:choose>
                    <div class="meta">
                        <div class="name"><c:out value="${item.displayName}"/></div>
                        <div class="sub">
                            <span class="badge"><c:out value="${item.kindLabel}"/></span>
                            <c:out value="${item.sizeLabel}"/>
                        </div>
                        <div class="sub storage-key"><c:out value="${item.key}"/></div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
