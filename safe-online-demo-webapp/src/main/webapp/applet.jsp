<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>SafeOnline Demo Web Application</title>
<script type="text/javascript" src="applet.js" />

</head>
<body onload="prepareCallback();">
<h1>Demo Applet</h1>
<applet name="DemoApplet"
	code="net.link.safeonline.demo.DemoApplet.class"
	archive="safe-online-demo-applet.jar" width="400" height="400">
	<param name="param1" value="value1" />
	<param name="sessionid" value="<%=session.getId() %>"/>
</applet>
<form name="demoForm" action="result.jsp"></form>
</body>
</html>
