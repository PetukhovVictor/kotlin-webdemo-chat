package com.jetbrains.web;

import com.jetbrains.domain.UserEntity;
import com.jetbrains.dao.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main(HttpServletRequest request) {
        UserDAO user = new UserDAO();
        UserEntity userInfo = user.getUserBySession(request.getSession());
        user.signIn(user.getUserById(8), request.getSession());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("content", "Kotlin web-demo chat");
        modelAndView.addObject("user", userInfo);
        modelAndView.addObject("frontendPath", "http://localhost:8081/assets/");
        modelAndView.setViewName("index");
        return modelAndView;
    }
}