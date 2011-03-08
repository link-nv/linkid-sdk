<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head><title>logged in</title></head>

<body>
<p>logged in</p>


<jsp:useBean id="attributes" scope="request" class="net.link.safeonline.sdk.example.jsp.AttributeBean"/>

<jsp:setProperty name="attributes" property="session" value="<%= request.getSession() %>"  />


<ul>
    <c:forEach var="attribute" items="${attributes.testAttributes}" >
    <li>${attribute.key}:
        <ul>
            <c:forEach var="attributeValue" items="${attribute.value}">
            <li>${attributeValue}</li>
            </c:forEach>
        </ul>
    </li>
    </c:forEach>
</ul>

</body>

</html>