package com.jetbrains.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Вспомогательный класс для работы с URL.
 */
public class UrlUtils {
    /**
     * Получение полного адреса URL по объекту HttpServletRequest.
     * Пример: http://localhost:8080/oauth2callback
     *
     * @param request Объект HttpServletRequest, по которому необходимо определить адрес.
     *
     * @return Полный адрес URL.
     */
    public static String getFullUrl(HttpServletRequest request) {
        StringBuffer fullUrlBuf = request.getRequestURL();
        if (request.getQueryString() != null) {
            fullUrlBuf.append('?').append(request.getQueryString());
        }
        return fullUrlBuf.toString();
    }

    /**
     * Получение базовой части URL по объекту HttpServletRequest.
     * Пример: http://localhost:8080/
     *
     * @param request Объект HttpServletRequest, по которому необходимо определить адрес.
     *
     * @return Базовая часть адреса URL.
     */
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int portNumber = request.getServerPort();
        String urlPortPart = portNumber != 80 ? (":" + portNumber) : "";
        return scheme + "://" + serverName + urlPortPart;
    }

    /**
     * Попытка и получение числового get-параметра запроса по объекту HttpServletRequest.
     *
     * @param request Объект HttpServletRequest, по которому необходимо получить параметр.
     * @param param Название параметра.
     *
     * @return Запрошенный числовой get-параметр (либо null, в случе отсутствия или ошибки парсинга).
     */
    public static Integer getIntegerParam(HttpServletRequest request, String param) {
        try {
            String stringParam = request.getParameter(param);
            return stringParam == null ? null : Integer.parseInt(stringParam);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}