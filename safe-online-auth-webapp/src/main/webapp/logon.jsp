<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<jsp:useBean id="logon"
	class="net.link.safeonline.auth.webapp.LogonBean" />
<jsp:setProperty name="logon" property="application"
	value="${application}" />
<jsp:setProperty name="logon" property="target" value="${target}" />
<jsp:setProperty name="logon" property="username" />
<jsp:setProperty name="logon" property="password" />
<%
logon.logon();
%>
<script type="text/javascript">
			<!--
			redirectionTime = "1000";
			redirectionUrl = "<%=logon.getTarget()%>?username=" + "<%=logon.getUsername()%>" 
				+ "&authenticated=" + "<%=logon.isAuthenticated()%>";
			
			function redirectionTimer() {
				self.setTimeout("self.location.href = redirectionUrl;", redirectionTime);
			}
			// -->
		</script>
<c:choose>
	<c:when test="${logon.authenticated}">
		<body onLoad="redirectionTimer()">
		<h1>Authenticated. Redirecting to application <jsp:getProperty
			name="logon" property="application" /> ...</h1>
		</body>
	</c:when>
	<c:otherwise>
		<body>
		<h1>Not Authenticated. Try again.</h1>
		</body>
	</c:otherwise>
</c:choose>
</html>
