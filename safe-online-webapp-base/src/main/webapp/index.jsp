<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:choose>
	<c:when test="${!empty param.authenticationTimeout}">
		<c:redirect url="/main.seam?authenticationTimeout=true" />
	</c:when>
	<c:otherwise>
		<c:redirect url="/main.seam" />
	</c:otherwise>
</c:choose>