<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>

<table border="1">
    <thead>
    <tr>
        <th>vězeň</th>
        <th>narozen</th>
    </tr>
    </thead>
    <c:forEach items="${prisoner}" var="prisoner">
    <tr>
        <td><c:out value="${prisoner.name}"/></td>
        <td><c:out value="${prisoner.born}"/></td>
        <td><form method="post" action="${pageContext.request.contextPath}/prisoner/delete?id=${prisoner.id}"
                  style="margin-bottom: 0;"><input type="submit" value="Smazat"></form></td>
    </tr>
    </c:forEach>
    </table>

    <h2>Zadejte vězně</h2>
    <c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
    <c:out value="${chyba}"/>
    </div>
    </c:if>
    <form action="${pageContext.request.contextPath}/prisoner/add" method="post">
    <table>
    <tr>
        <th>jméno vězně:</th>
        <td><input type="text" name="name" value="<c:out value='${param.name}'/>"/></td>
    </tr>
    <tr>
        <th>datum narození:</th>
        <td><input type="date" name="born" value="<c:out value='${param.born}'/>"/></td>
    </tr>
</table>
<input type="Submit" value="Zadat" />
</form>
</body>
</html>