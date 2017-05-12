package com.jetbrains.utils;

import javax.servlet.http.HttpServletRequest;

public class UrlString {
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
}