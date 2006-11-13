<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>Logon</title>
</head>
<body>
<h1>Logon</h1>
<form method="POST" action="j_security_check">
	<table>
		<tr>
			<th>Username:</th>
			<td><input type="text" name="j_username"/></td>
		</tr>
		<tr>
			<th>Password:</th>
			<td><input type="password" name="j_password"/></td>
		</tr>
	</table>
	<input type="submit" value="Logon"/> 
	<input type="reset" value="Reset"/>
</form>
</body>
</html>
