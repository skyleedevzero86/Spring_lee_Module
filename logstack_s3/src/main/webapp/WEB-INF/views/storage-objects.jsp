<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${browse.bucketDisplayName}"/>
<c:set var="headerSecondaryActionUrl" value="/storage"/>
<c:set var="headerSecondaryActionLabel" value="버킷 목록"/>
<c:set var="headerActionUrl" value="/"/>
<c:set var="headerActionLabel" value="파일 목록"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<div class="storage-summary">
    버킷 <strong><c:out value="${browse.bucketCode}"/></strong>
    · 리전 <c:out value="${browse.region}"/>
    · 객체 <strong>${browse.page.totalElements}</strong>건
</div>

<form class="search" method="get" action="<c:url value='/storage/buckets/${browse.bucketCode}'/>">
    <input type="text" name="keyword" value="<c:out value='${keyword}'/>" placeholder="키·파일명 검색">
    <input type="hidden" name="prefix" value="${prefix}">
    <input type="hidden" name="size" value="${pageSize}">
    <button type="submit" class="btn">검색</button>
    <a class="btn btn-secondary" href="<c:url value='/storage/buckets/${browse.bucketCode}'/>?prefix=${prefix}&amp;size=${pageSize}">새로고침</a>
    <c:if test="${not empty keyword}">
        <a class="btn btn-secondary" href="<c:url value='/storage/buckets/${browse.bucketCode}'/>?prefix=${prefix}&amp;size=${pageSize}">초기화</a>
    </c:if>
</form>

<nav class="storage-tabs">
    <c:url var="objectBase" value="/storage/buckets/${browse.bucketCode}"/>
    <a class="storage-tab ${prefix == 'all' ? 'active' : ''}" href="${objectBase}?prefix=all&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">전체</a>
    <a class="storage-tab ${prefix == 'uploads' ? 'active' : ''}" href="${objectBase}?prefix=uploads&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">uploads</a>
    <a class="storage-tab ${prefix == 'thumbnails' ? 'active' : ''}" href="${objectBase}?prefix=thumbnails&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">thumbnails</a>
</nav>

<c:choose>
    <c:when test="${empty browse.page.content}">
        <div class="empty">조건에 맞는 S3 객체가 없습니다.</div>
    </c:when>
    <c:otherwise>
        <div class="grid">
            <c:forEach var="item" items="${browse.page.content}">
                <article class="card storage-card">
                    <c:choose>
                        <c:when test="${item.image and not empty item.previewUrl}">
                            <c:url var="previewLink" value="/storage/buckets/${browse.bucketCode}/preview">
                                <c:param name="key" value="${item.key}"/>
                            </c:url>
                            <a href="${previewLink}">
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

<c:if test="${browse.page.totalPages > 0}">
    <nav class="pager">
        <c:url var="objectUrl" value="/storage/buckets/${browse.bucketCode}"/>
        <c:choose>
            <c:when test="${currentPage == 0}">
                <span class="disabled">이전</span>
            </c:when>
            <c:otherwise>
                <a href="${objectUrl}?page=${currentPage - 1}&amp;size=${pageSize}&amp;prefix=${prefix}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">이전</a>
            </c:otherwise>
        </c:choose>
        <c:forEach begin="0" end="${browse.page.totalPages - 1}" var="i">
            <c:choose>
                <c:when test="${i == currentPage}">
                    <span class="current">${i + 1}</span>
                </c:when>
                <c:otherwise>
                    <a href="${objectUrl}?page=${i}&amp;size=${pageSize}&amp;prefix=${prefix}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">${i + 1}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:choose>
            <c:when test="${currentPage >= browse.page.totalPages - 1}">
                <span class="disabled">다음</span>
            </c:when>
            <c:otherwise>
                <a href="${objectUrl}?page=${currentPage + 1}&amp;size=${pageSize}&amp;prefix=${prefix}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">다음</a>
            </c:otherwise>
        </c:choose>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
