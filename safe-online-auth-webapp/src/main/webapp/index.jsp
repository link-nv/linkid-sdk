<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="applicationId" value="${param.application}" scope="session" />
<c:set var="target" value="${param.target}" scope="session" />
<c:redirect url="/main.seam" />
