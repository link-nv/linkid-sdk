<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>${initParam.title}</title>
</head>
<body>
<h1>${initParam.title}</h1>

<p>Welcome to the ${initParam.title}.</p>

<p>Click <a href="./secure/">here</a> to visit the secured section
of the site. This will trigger container-managed authentication via
application/container-collected credentials.</p>

<p>Click <a href="./secure2/">here</a> to visit a secured section of
the site. This will trigger application-managed authentication via
authentication service-collected credentials.</p>

<p>Click <a href="./applet.jsp">here</a> to see a demo Java Applet
in action.</p>

</body>
</html>
