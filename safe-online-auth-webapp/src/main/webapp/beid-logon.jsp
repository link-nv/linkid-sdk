<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<script type="text/javascript">
			<!--
			redirectionTime = "1000";
			redirectionUrl = "${target}?username=" + "${user}" 
				+ "&authenticated=true";
			
			function redirectionTimer() {
				self.setTimeout("self.location.href = redirectionUrl;", redirectionTime);
			}
			// -->
		</script>
<c:choose>
	<c:when test="${!empty user}">
		<body onLoad="redirectionTimer()">
		<h1>Authenticated. Redirecting to application ${application} ...
		</h1>
		</body>
	</c:when>
	<c:otherwise>
		<body>
		<h1>Not Authenticated. Try again.</h1>
		</body>
	</c:otherwise>
</c:choose>
</html>
