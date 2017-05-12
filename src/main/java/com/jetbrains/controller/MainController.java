package com.jetbrains.controller;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.service.GoogleOAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.jetbrains.utils.UrlString;

@Controller
public class MainController {
    private ModelAndView oAuthError() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("oauth/fail");
        return modelAndView;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("content", "Kotlin web-demo chat");
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = GoogleOAuth.generateAuthorizeUrl(request.getRequestURL().toString());
        response.sendRedirect(url);
    }

    @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request) throws IOException {
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

        return null;
    }
}