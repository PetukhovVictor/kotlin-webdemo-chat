package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dao.GoogleOAuth;
import com.jetbrains.dao.UserDAO;
import com.jetbrains.util.UrlString;
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
    private ModelAndView oAuthError() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("oauth/fail");
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
        UserDAO user = new UserDAO();
        UserEntity userInfo = user.getUserBySession(session);
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
        UserDAO user = new UserDAO();
        user.sign(userInfo, request.getSession());
        response.sendRedirect("/");
        return null;
    }

    @ResponseBody
    @RequestMapping(
            value = "/rest/user_info",
            method = RequestMethod.GET,
            produces = { "application/json;charset=UTF-8" })
    public String userInfo(HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserEntity user = (new UserDAO()).getUserBySession(request.getSession());
        return mapper.writeValueAsString(user);
    }
}
