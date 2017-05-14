package com.jetbrains.controller;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.model.UsersEntity;
import com.jetbrains.service.GoogleOAuth;
import com.jetbrains.service.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import com.jetbrains.util.UrlString;

@Controller
public class MainController {
    private ModelAndView oAuthError() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("oauth/fail");
        return modelAndView;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main(HttpServletRequest request) {
        User user = new User();
        UsersEntity userInfo = user.getUserBySession(request.getSession());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("content", "Kotlin web-demo chat");
        modelAndView.addObject("user", userInfo);
        modelAndView.addObject("frontendPath", "http://localhost:8081/assets/");
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = GoogleOAuth.generateAuthorizeUrl(request.getRequestURL().toString());
        response.sendRedirect(url);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = new User();
        UsersEntity userInfo = user.getUserBySession(session);
        if (userInfo == null) {
            response.sendRedirect("/");
            return;
        }
        user.logout(session);
        response.sendRedirect("/");
    }

    @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUrl = UrlString.getFullUrl(request);
        // Валидируем строку запроса на наличие code.
        if (!GoogleOAuth.codeValidate(requestUrl)) {
            return oAuthError();
        }
        String requestBaseUrl = UrlString.getBaseUrl(request);
        String code = request.getParameter("code");
        String token = GoogleOAuth.requestAccessToken(requestBaseUrl, code);
        if (token == null) {
            return oAuthError();
        }
        Userinfoplus userInfo = GoogleOAuth.getUserInfo(token);
        User user = new User();
        user.sign(userInfo, request.getSession());
        response.sendRedirect("/");
        return null;
    }
}