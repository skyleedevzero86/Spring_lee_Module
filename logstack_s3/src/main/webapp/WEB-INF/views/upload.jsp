<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="파일 업로드"/>
<%@ include file="/WEB-INF/views/includes/layout-top.jsp" %>

<div class="panel">
    <form method="post" action="<c:url value='/upload'/>" enctype="multipart/form-data">
        <input type="file" name="file" required>
        <button type="submit" class="btn">업로드</button>
    </form>
    <a class="back-link" href="<c:url value='/'/>">목록으로</a>
</div>

<%@ include file="/WEB-INF/views/includes/layout-bottom.jsp" %>
