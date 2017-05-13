<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE HTML>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Main page</title>
        <link rel="stylesheet" href="${frontendPath}app.css" />
        <c:choose>
            <c:when test="${user != null}"><link rel="stylesheet" href="${frontendPath}app.css" /><</c:when>
            <c:otherwise><link rel="stylesheet" href="${frontendPath}login.css" /></c:otherwise>
        </c:choose>
    </head>
    <body>
        <c:choose>
            <c:when test="${user != null}">
                <p>Привет, ${user.name}</p>
                <p><a href="/logout">Выйти</a></p>
            </c:when>
            <c:otherwise>
                <div class="login-block">
                    <div class="login-description">Добро пожаловать в Kotlin web-demo!</div>
                    <p><a href="/authorize" title="Войти через Google">Войти через Google</a></p>
                </div>
            </c:otherwise>
        </c:choose>
        <p>${content}</p>
    </body>
</html>
