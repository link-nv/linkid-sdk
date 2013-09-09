<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>linkID Mobile Example - authenticated</title>
    </head>

<body>

<jsp:useBean id="attributes" scope="request" class="net.link.safeonline.sdk.example.mobile.AttributeBean"/>

<jsp:setProperty name="attributes" property="session" value="<%= request.getSession() %>"  />

<h1>Logged in with userId=${attributes.userId}</h1>

<ul>
    <c:forEach var="attribute" items="${attributes.attributes}" >
    <li>${attribute.key}:
        <ul>
            <c:forEach var="attributeValue" items="${attribute.value}">
            <li>${attributeValue} : ${attributeValue.value}</li>
            </c:forEach>
        </ul>
    </li>
    </c:forEach>
</ul>

</body>

</html>