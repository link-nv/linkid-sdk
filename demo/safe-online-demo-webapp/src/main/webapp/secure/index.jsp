<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>${initParam.title}</title>
</head>
<body>
<h1>${initParam.title}</h1>
<p>Welcome <%= request.getUserPrincipal().getName() %> to the ${initParam.title}.</p>
<p>This page demonstrates the servlet container security features.</p>
<form method="get" action="../logout.jsp">
	<input type="submit" value="Logout"/>
</form>
</body>
</html>
