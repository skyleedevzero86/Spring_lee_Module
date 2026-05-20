<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="site-header">
    <h1><c:out value="${pageTitle}"/></h1>
    <div class="header-actions">
        <c:if test="${not empty headerSecondaryActionUrl}">
            <a class="btn btn-secondary" href="<c:url value='${headerSecondaryActionUrl}'/>"><c:out value="${headerSecondaryActionLabel}"/></a>
        </c:if>
        <c:if test="${not empty headerActionUrl}">
            <a class="btn" href="<c:url value='${headerActionUrl}'/>"><c:out value="${headerActionLabel}"/></a>
        </c:if>
    </div>
</header>
