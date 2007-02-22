<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>${initParam.title}</title>
</head>
<body bgcolor="#aaffaa">
<h1>${initParam.title}</h1>
<p>Welcome to the ${initParam.title}.</p>
<p>Login: <%=session.getAttribute("username")%></p>
<p>This page demonstrates application-managed authentication via
authentication service-collected credentials.</p>
<p><a href="./attrib.jsp">Attribute Demo</a></p>
<form method="get" action="../logout.jsp"><input type="submit"
	value="Logout" /></form>
</body>
</html>
