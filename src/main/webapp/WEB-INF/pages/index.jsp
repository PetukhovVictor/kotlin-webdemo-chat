<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE HTML>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Kotlin webdemo chat</title>
        <link rel="stylesheet" href="${frontendPath}main.css" />
    </head>
    <body>
        <c:choose>
            <c:when test="${user != null}">
                <div id="chat-container"></div>
                <script type="text/javascript" src="${frontendPath}main.js"></script>
            </c:when>
            <c:otherwise>
                <div class="login-block">
                    <div class="login-description">Добро пожаловать в Kotlin web-demo!</div>
                    <a href="/authorize" class="login-link">Войти через Google</a>
                </div>
            </c:otherwise>
        </c:choose>
    </body>
</html>
