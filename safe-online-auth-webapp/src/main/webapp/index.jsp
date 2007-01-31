<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<body>
<c:set var="application" value="${param.application}" scope="session" />
<c:set var="target" value="${param.target}" scope="session" />
<h1>SafeOnline Authentication Web Application</h1>
<table>
	<tr>
		<th>Application:</th>
		<td>${application}</td>
	</tr>
	<tr>
		<th>Target:</th>
		<td>${target}</td>
	</tr>
</table>
<p>Please select an authentication device:</p>
<ul>
	<li><a href="./username-password.jsp">Username/password</a></li>
	<li><a href="./beid.jsp">BeID</a></li>
</ul>
</body>
</html>
