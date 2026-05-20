<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="파일 목록"/>
<c:set var="headerSecondaryActionUrl" value="/storage"/>
<c:set var="headerSecondaryActionLabel" value="스토리지 보기"/>
<c:set var="headerActionUrl" value="/upload"/>
<c:set var="headerActionLabel" value="업로드"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<c:if test="${not empty message}">
    <div class="alert alert-success"><c:out value="${message}"/></div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-error"><c:out value="${error}"/></div>
</c:if>

<form class="search" method="get" action="<c:url value='/'/>">
    <input type="text" name="keyword" value="<c:out value='${keyword}'/>" placeholder="파일명 검색">
    <input type="hidden" name="size" value="${pageSize}">
    <button type="submit" class="btn">검색</button>
    <c:if test="${not empty keyword}">
        <a class="btn btn-secondary" href="<c:url value='/'/>?size=${pageSize}">초기화</a>
    </c:if>
</form>

<c:choose>
    <c:when test="${empty page.content}">
        <div class="empty">등록된 파일이 없습니다.</div>
    </c:when>
    <c:otherwise>
        <div class="grid">
            <c:forEach var="item" items="${page.content}">
                <article class="card">
                    <a href="<c:url value='/files/${item.id}'/>">
                        <c:choose>
                            <c:when test="${not empty item.thumbnailUrl}">
                                <img class="thumb" src="<c:out value='${item.thumbnailUrl}'/>" alt="">
                            </c:when>
                            <c:otherwise>
                                <div class="thumb"></div>
                            </c:otherwise>
                        </c:choose>
                        <div class="meta">
                            <div class="name"><c:out value="${item.originalFilename}"/></div>
                            <div class="sub"><c:out value="${item.mediaType}"/> · <c:out value="${item.bucketDisplayName}"/> · <c:out value="${item.sizeLabel}"/></div>
                        </div>
                    </a>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${page.totalPages > 0}">
    <nav class="pager">
        <c:url var="listUrl" value="/"/>
        <c:choose>
            <c:when test="${currentPage == 0}">
                <span class="disabled">이전</span>
            </c:when>
            <c:otherwise>
                <a href="${listUrl}?page=${currentPage - 1}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">이전</a>
            </c:otherwise>
        </c:choose>

        <c:forEach begin="0" end="${page.totalPages - 1}" var="i">
            <c:choose>
                <c:when test="${i == currentPage}">
                    <span class="current">${i + 1}</span>
                </c:when>
                <c:otherwise>
                    <a href="${listUrl}?page=${i}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">${i + 1}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>

        <c:choose>
            <c:when test="${currentPage >= page.totalPages - 1}">
                <span class="disabled">다음</span>
            </c:when>
            <c:otherwise>
                <a href="${listUrl}?page=${currentPage + 1}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">다음</a>
            </c:otherwise>
        </c:choose>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
