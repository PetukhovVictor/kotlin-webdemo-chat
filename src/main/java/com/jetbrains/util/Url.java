package com.jetbrains.util;

import javax.servlet.http.HttpServletRequest;

public class Url {
    public static String getFullUrl(HttpServletRequest request) {
        StringBuffer fullUrlBuf = request.getRequestURL();
        if (request.getQueryString() != null) {
            fullUrlBuf.append('?').append(request.getQueryString());
        }
        return fullUrlBuf.toString();
    }
    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int portNumber = request.getServerPort();
        String urlPortPart = portNumber != 80 ? (":" + portNumber) : "";
        return scheme + "://" + serverName + urlPortPart;
    }
    public static Integer getPageParam(HttpServletRequest request, Integer defaultPage) {
        Integer page = Url.getIntegerParam(request, "page");
        return page == null ? defaultPage : page;
    }
    public static Integer getPageParam(HttpServletRequest request) {
        return Url.getPageParam(request, 1);
    }
    public static Integer getIntegerParam(HttpServletRequest request, String param) {
        try {
            String stringParam = request.getParameter(param);
            return stringParam == null ? null : Integer.parseInt(stringParam);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}