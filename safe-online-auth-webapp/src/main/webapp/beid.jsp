<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>SafeOnline Authentication Web Application</title>
</head>
<body>
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

<applet name="AuthenticationApplet"
	code="net.link.safeonline.auth.AuthenticationApplet.class"
	archive="safe-online-auth-applet-package.jar" width="450" height="400">
	<param name="SmartCardConfig" value="beid" />
	<param name="ServletPath" value="authentication/" />
	<param name="TargetPath" value="beid-logon.jsp" />
	<param name="SessionId" value="<%= session.getId() %>" />
	<param name="ApplicationId" value="${application}" />
</applet>

</body>
</html>
