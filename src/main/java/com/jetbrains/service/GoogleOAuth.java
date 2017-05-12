package com.jetbrains.service;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Осуществление авторизации пользователя через Google-аккаунт по oAuth2.
 * Используется библиотека google-oauth-client.
 */
public class GoogleOAuth {
    /**
     * Ключевой URL, используемый при авторизации.
     */
    private static final String URL = "https://accounts.google.com/o/oauth2";

    /**
     * URL, на который будет осуществлено перенаправления пользователя после подтверждения авторизации через свой аккаунт.
     */
    private static final String REDIRECT_URI = "/oauth2callback";

    /**
     * Идентификатор клиента oAuth2.
     */
    private static final String CLIENT_ID = "986611741361-ae8ec8k5e3h79220mua7pkmopp91moqn.apps.googleusercontent.com";

    /**
     * Идентификатор клиента oAuth2.
     */
    private static final String CLIENT_SECRET = "ZH8RAErJH87c99IIttJK3TiS";

    /**
     * Пространства, к которым запрашивается доступ при авторизации.
     */
    private static final String[] SCOPES = {
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile"
    };

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

    /**
     * Генерация URL для редиректа на страницу авторизации через Google account.
     *
     * @param requestUrl Адрес запрошенной страницы (для дальнейшего включения в redirect URI).
     *
     * @return Конечный URL для перенаправления на авторизацию через Google account.
     */
    public static String generateAuthorizeUrl(String requestUrl) {
        return new BrowserClientRequestUrl(GoogleOAuth.URL + "/auth", GoogleOAuth.CLIENT_ID)
                .setResponseTypes(Arrays.asList("code"))
                .setScopes(Arrays.asList(GoogleOAuth.SCOPES))
                .setRedirectUri(GoogleOAuth.getRedirectUri(requestUrl)).build();
    }

    /**
     * Генерация URL для редиректа на страницу авторизации через Google account.
     *
     * @param requestUrl Адрес запрошенной страницы (для дальнейшего включения в redirect URI).
     *
     * @return Конечный URL для перенаправления на авторизацию через Google account.
     */
    public static boolean codeValidate(String requestUrl) {
        AuthorizationCodeResponseUrl authResponse = new AuthorizationCodeResponseUrl(requestUrl);
        return authResponse.getError() == null;
    }

    /**
     * Получение токена для дальнейшего общения с сервисами Google API.
     *
     * @param requestUrl Базовый URL-адрес сервера (без path).
     * @param code Code, полученный после подтверждения доступа пользователем.
     *
     * @return Токен для дальнешего общения с Google API (либо null, в случае неудачи)
     *
     * @throws IOException
     */
    public static String requestAccessToken(String requestUrl, String code) throws IOException {
        try {
            TokenResponse response =
                    new AuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            new JacksonFactory(),
                            new GenericUrl(GoogleOAuth.URL + "/token"),
                            code)
                        .setClientAuthentication(new BasicAuthentication(GoogleOAuth.CLIENT_ID, GoogleOAuth.CLIENT_SECRET))
                        .setRedirectUri(GoogleOAuth.getRedirectUri(requestUrl))
                        .execute();
            return response.getAccessToken();
        } catch (TokenResponseException e) {
            return null;
        }
    }
}
