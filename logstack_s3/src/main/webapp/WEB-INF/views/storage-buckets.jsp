<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="스토리지 버킷"/>
<c:set var="headerActionUrl" value="/"/>
<c:set var="headerActionLabel" value="파일 목록"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<form class="search" method="get" action="<c:url value='/storage'/>">
    <input type="text" name="keyword" value="<c:out value='${keyword}'/>" placeholder="버킷 코드·표시명 검색">
    <input type="hidden" name="size" value="${pageSize}">
    <button type="submit" class="btn">검색</button>
    <a class="btn btn-secondary" href="<c:url value='/storage'/>?size=${pageSize}">새로고침</a>
    <c:if test="${not empty keyword}">
        <a class="btn btn-secondary" href="<c:url value='/storage'/>?size=${pageSize}">초기화</a>
    </c:if>
</form>

<c:choose>
    <c:when test="${empty page.content}">
        <div class="empty">등록된 스토리지 버킷이 없습니다.</div>
    </c:when>
    <c:otherwise>
        <div class="board-list">
            <c:forEach var="bucket" items="${page.content}">
                <a class="board-item" href="<c:url value='/storage/buckets/${bucket.bucketCode}'/>">
                    <div class="board-item-title"><c:out value="${bucket.displayName}"/></div>
                    <div class="board-item-sub">
                        <c:out value="${bucket.bucketCode}"/> · <c:out value="${bucket.region}"/>
                    </div>
                </a>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${page.totalPages > 0}">
    <nav class="pager">
        <c:url var="baseUrl" value="/storage"/>
        <c:choose>
            <c:when test="${currentPage == 0}">
                <span class="disabled">이전</span>
            </c:when>
            <c:otherwise>
                <a href="${baseUrl}?page=${currentPage - 1}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">이전</a>
            </c:otherwise>
        </c:choose>
        <c:forEach begin="0" end="${page.totalPages - 1}" var="i">
            <c:choose>
                <c:when test="${i == currentPage}">
                    <span class="current">${i + 1}</span>
                </c:when>
                <c:otherwise>
                    <a href="${baseUrl}?page=${i}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">${i + 1}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:choose>
            <c:when test="${currentPage >= page.totalPages - 1}">
                <span class="disabled">다음</span>
            </c:when>
            <c:otherwise>
                <a href="${baseUrl}?page=${currentPage + 1}&amp;size=${pageSize}<c:if test='${not empty keyword}'>&amp;keyword=${fn:escapeXml(keyword)}</c:if>">다음</a>
            </c:otherwise>
        </c:choose>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
