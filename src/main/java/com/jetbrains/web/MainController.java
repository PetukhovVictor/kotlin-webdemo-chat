package com.jetbrains.web;

import com.jetbrains.AppConfig;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.UserDTO;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@Controller
public class MainController {
    /**
     * Обработка запроса главной страницы приложения.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main(HttpServletRequest request) throws FileNotFoundException, ParseException, IOException {
        UserDTO userInfo = new UserDAOImpl().getCurrentUser(request.getSession());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("content", "Kotlin web-demo chat");
        modelAndView.addObject("user", userInfo);
        modelAndView.addObject("frontendPath", AppConfig.FRONTEND_PATH);
        modelAndView.setViewName("index");
        return modelAndView;
    }
}