package com.jetbrains.controller;

import com.jetbrains.model.UsersEntity;
import com.jetbrains.service.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {

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
}