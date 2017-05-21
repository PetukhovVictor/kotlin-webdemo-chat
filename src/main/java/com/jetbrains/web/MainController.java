package com.jetbrains.web;

import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    /**
     * Обработка запроса главной страницы приложения.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main(HttpServletRequest request) {
        UserDAOImpl user = new UserDAOImpl();
        UserDTO userInfo = user.getCurrentUser(request.getSession());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("content", "Kotlin web-demo chat");
        modelAndView.addObject("user", userInfo);
        modelAndView.addObject("frontendPath", "http://localhost:8081/assets/");
        modelAndView.setViewName("index");
        return modelAndView;
    }
}