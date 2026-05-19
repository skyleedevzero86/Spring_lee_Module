<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${file.originalFilename}"/>
<c:set var="headerActionUrl" value="/"/>
<c:set var="headerActionLabel" value="목록"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<div class="panel-wide">
    <div class="panel" style="max-width:none;margin:0;">
        <div class="detail-meta">
            <c:out value="${file.contentType}"/> · ${file.size} bytes · ${file.createdAt}
        </div>

        <div class="preview">
            <c:choose>
                <c:when test="${file.image}">
                    <img src="<c:out value='${file.previewUrl}'/>" alt="">
                </c:when>
                <c:when test="${file.pdf}">
                    <iframe src="<c:out value='${file.previewUrl}'/>" title="PDF preview"></iframe>
                </c:when>
                <c:otherwise>
                    <div class="fallback">미리보기를 지원하지 않는 형식입니다. 다운로드로 확인하세요.</div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="actions">
            <a class="btn" href="<c:out value='${file.downloadUrl}'/>" download>다운로드</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
