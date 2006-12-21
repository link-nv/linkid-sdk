<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>SafeOnline Demo Web Application</title>
</head>
<body>
<h1>SafeOnline Demo Web Application</h1>
<p>Welcome to the SafeOnline Demo Web Application.</p>
<p>Login: <%=session.getAttribute("username")%></p>
<p>This page demonstrates application-managed authentication via
authentication service-collected credentials.</p>
<form method="get" action="../logout.jsp"><input type="submit"
	value="Logout" /></form>
</body>
</html>
