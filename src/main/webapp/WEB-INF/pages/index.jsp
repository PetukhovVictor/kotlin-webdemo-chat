<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=utf-8" %>
<html>
    <head>
        <title>Main page</title>
    </head>
    <body>
        <c:choose>
            <c:when test="${user != null}">
                <p>Привет, ${user.name}</p>
                <p><a href="/logout">Выйти</a></p>
            </c:when>
            <c:otherwise>
                <p><a href="/authorize" title="Войти через Google">Войти через Google</a></p>
            </c:otherwise>
        </c:choose>
        <p>${content}</p>
    </body>
</html>
