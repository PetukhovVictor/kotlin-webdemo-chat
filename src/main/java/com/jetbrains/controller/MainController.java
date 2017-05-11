package com.jetbrains.controller;

import com.jetbrains.service.GoogleOAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class MainController {
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
}