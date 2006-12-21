<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<jsp:useBean id="logon"
	class="net.link.safeonline.auth.webapp.LogonBean" />
<jsp:setProperty name="logon" property="application" />
<jsp:setProperty name="logon" property="target" />
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
<body onLoad="redirectionTimer()">
<c:choose>
	<c:when test="${logon.authenticated}">
		<h1>Authenticated. Redirecting to application <jsp:getProperty
			name="logon" property="application" /> ...</h1>
	</c:when>
	<c:otherwise>
		<h1>Not Authenticated. Try again.</h1>
	</c:otherwise>
</c:choose>
</body>
</html>
