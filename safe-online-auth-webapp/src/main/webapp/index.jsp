<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<body>
<h1>SafeOnline Authentication Web Application</h1>
<form action="./logon.jsp">
<table>
	<tr>
		<th>Application:</th>
		<td><%= request.getParameter("application") %></td>
	</tr>
	<tr>
		<th>Target:</th>
		<td><%= request.getParameter("target") %></td>
	</tr>
	<tr>
		<th>Username:</th>
		<td><input type="text" name="username" /></td>
	</tr>
	<tr>
		<th>Password:</th>
		<td><input type="password" name="password" /></td>
	</tr>
</table>
<input type="submit" value="Logon" /></form>
</body>
</html>
