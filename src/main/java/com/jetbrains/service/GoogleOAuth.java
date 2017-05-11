package com.jetbrains.service;

import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;
import com.google.api.client.http.GenericUrl;

import java.util.Arrays;

/**
 * Осуществление авторизации пользователя через Google-аккаунт по oAuth2.
 * Используется библиотека google-oauth-client.
 */
public class GoogleOAuth {
    /**
     * Ключевой URL, используемый при авторизации.
     */
    private static final String URL = "https://accounts.google.com/o/oauth2/auth";

    /**
     * URL, на который будет осуществлено перенаправления пользователя после подтверждения авторизации через свой аккаунт.
     */
    private static final String REDIRECT_URI = "/oauth2callback";

    /**
     * Идентификатор клиента oAuth2.
     */
    private static final String CLIENT_ID = "986611741361-ae8ec8k5e3h79220mua7pkmopp91moqn.apps.googleusercontent.com";

    /**
     * Пространства, к которым запрашивается доступ при авторизации.
     */
    private static final String[] SCOPES = {
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile"
    };

    /**
     * Генерация URL для редиректа на страницу авторизации через Google account.
     *
     * @param requestUrl Адрес запрошенной страницы (для дальнейшего включения в redirect URI).
     *
     * @return Конечный URL для перенаправления на авторизацию через Google account.
     */
    public static String generateAuthorizeUrl(String requestUrl) {
        return new BrowserClientRequestUrl(GoogleOAuth.URL, GoogleOAuth.CLIENT_ID)
                .setResponseTypes(Arrays.asList("code"))
                .setScopes(Arrays.asList(GoogleOAuth.SCOPES))
                .setRedirectUri(GoogleOAuth.getRedirectUri(requestUrl)).build();
    }

    /**
     * Генерация redirect URI для последующей вставки в authorize URL
     * (для перенаправления пользователя обратно после подтверждения доступа).
     *
     * @param requestUrl Адрес запрошенной страницы (для включения в redirect URI).
     *
     * @return redirect URI.
     */
    private static String getRedirectUri(String requestUrl) {
        GenericUrl url = new GenericUrl(requestUrl);
        url.setRawPath(GoogleOAuth.REDIRECT_URI);
        return url.build();
    }
}
