<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<title>${initParam.title}</title>
</head>
<body bgcolor="#aaffaa">
<h1>${initParam.title}</h1>
<p>Login: <%=session.getAttribute("username")%></p>
<jsp:useBean id="attrib"
	class="net.link.safeonline.demo.webapp.AttributeBean" />
<jsp:setProperty name="attrib" property="attributeName"
	value="urn:net:lin-k:safe-online:attribute:name" />
<jsp:setProperty name="attrib" property="subjectLogin"
	value="<%=session.getAttribute("username")%>" />
<jsp:setProperty name="attrib" property="attributeWebServiceLocation"
	value="localhost" />
<p>Attribute value: <jsp:getProperty name="attrib"
	property="attributeValue" /></p>
<form method="get" action="./"><input type="submit" value="Back" /></form>
</body>
</html>

