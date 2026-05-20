<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${preview.displayName}"/>
<c:set var="headerSecondaryActionUrl" value="/storage/buckets/${preview.bucketCode}"/>
<c:set var="headerSecondaryActionLabel" value="객체 목록"/>
<c:set var="headerActionUrl" value="/storage"/>
<c:set var="headerActionLabel" value="버킷 목록"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<div class="panel-wide">
    <div class="panel" style="max-width:none;margin:0;">
        <div class="detail-meta">
            <c:out value="${preview.bucketDisplayName}"/> · <c:out value="${preview.bucketCode}"/>
        </div>
        <div class="detail-meta storage-key">
            <c:out value="${preview.objectKey}"/>
            <c:if test="${preview.originalKey != preview.objectKey}">
                → <c:out value="${preview.originalKey}"/>
            </c:if>
        </div>
        <div class="preview">
            <c:choose>
                <c:when test="${preview.image and not empty preview.previewUrl}">
                    <img src="<c:out value='${preview.previewUrl}'/>" alt="">
                </c:when>
                <c:otherwise>
                    <div class="fallback">이미지 미리보기를 지원하지 않는 객체입니다.</div>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${preview.image and not empty preview.previewUrl}">
            <div class="actions">
                <a class="btn" href="<c:out value='${preview.previewUrl}'/>" target="_blank" rel="noopener">원본 새 탭</a>
            </div>
        </c:if>
    </div>
</div>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
