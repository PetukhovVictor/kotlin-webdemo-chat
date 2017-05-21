package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.AppConfig;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.GoogleOAuthUtils;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.util.ResponseError;
import com.jetbrains.util.UrlUtils;
import com.jetbrains.web.errors.ResponseErrors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class UserController {
    /**
     * Генерация представления страницы с ошибкой авторизации.
     *
     * @return Сгенерированное представление.
     */
    private ModelAndView oAuthError() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("oauth/fail");
        modelAndView.addObject("frontendPath", AppConfig.FRONTEND_PATH);
        return modelAndView;
    }

    /**
     * Обработка перехода на страницу авторизации.
     * Осуществляется переброс пользователя на страницу авторизации через Google аккаунт.
     */
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = GoogleOAuthUtils.generateAuthorizeUrl(request.getRequestURL().toString());
        response.sendRedirect(url);
    }

    /**
     * Обработка перехода на страницу выхода пользователя из системы.
     * После очистки сессии осуществляет редирект на главную страницу.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        UserDAOImpl user = new UserDAOImpl();
        if (user.getCurrentUser(session) == null) {
            response.sendRedirect(AppConfig.APP_INDEX_PAGE);
            return;
        }
        user.logout(session);
        response.sendRedirect(AppConfig.APP_INDEX_PAGE);
    }

    /**
     * Обработка перехода на страницу авторизации внутри приложения после авторизации на стороне Google.
     */
    @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUrl = UrlUtils.getFullUrl(request);
        // Валидируем строку запроса на наличие code.
        if (!GoogleOAuthUtils.codeValidate(requestUrl)) {
            return oAuthError();
        }
        String requestBaseUrl = UrlUtils.getBaseUrl(request);
        String code = request.getParameter("code");
        String token = GoogleOAuthUtils.requestAccessToken(requestBaseUrl, code);
        if (token == null) {
            return oAuthError();
        }
        Userinfoplus userInfo = GoogleOAuthUtils.getUserInfo(token);
        new UserDAOImpl().sign(userInfo, request.getSession());
        response.sendRedirect(AppConfig.APP_INDEX_PAGE);
        return null;
    }

    /**
     * Рест-метод получения информации о текущем пользователе.
     */
    @ResponseBody
    @RequestMapping(
            value = "/rest/user_info",
            method = RequestMethod.GET,
            produces = { "application/json;charset=UTF-8" })
    public String userInfo(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        return new ObjectMapper().writeValueAsString(user);
    }
}
