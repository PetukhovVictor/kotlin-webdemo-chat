package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.GoogleOAuth;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.util.Url;
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
        modelAndView.addObject("frontendPath", "http://localhost:8081/assets/");
        return modelAndView;
    }

    /**
     * Обработка перехода на страницу авторизации.
     * Осуществляется переброс пользователя на страницу авторизации через Google аккаунт.
     */
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = GoogleOAuth.generateAuthorizeUrl(request.getRequestURL().toString());
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
        UserDTO userInfo = user.getCurrentUser(session);
        if (userInfo == null) {
            response.sendRedirect("/");
            return;
        }
        user.logout(session);
        response.sendRedirect("/");
    }

    /**
     * Обработка перехода на страницу авторизации внутри приложения после авторизации на стороне Google.
     */
    @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUrl = Url.getFullUrl(request);
        // Валидируем строку запроса на наличие code.
        if (!GoogleOAuth.codeValidate(requestUrl)) {
            return oAuthError();
        }
        String requestBaseUrl = Url.getBaseUrl(request);
        String code = request.getParameter("code");
        String token = GoogleOAuth.requestAccessToken(requestBaseUrl, code);
        if (token == null) {
            return oAuthError();
        }
        Userinfoplus userInfo = GoogleOAuth.getUserInfo(token);
        new UserDAOImpl().sign(userInfo, request.getSession());
        response.sendRedirect("/");
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
    public String userInfo(HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        return mapper.writeValueAsString(user);
    }
}
