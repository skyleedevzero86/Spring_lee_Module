<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="site-header">
    <h1><c:out value="${pageTitle}"/></h1>
    <c:if test="${not empty headerActionUrl}">
        <a class="btn" href="<c:url value='${headerActionUrl}'/>"><c:out value="${headerActionLabel}"/></a>
    </c:if>
</header>
