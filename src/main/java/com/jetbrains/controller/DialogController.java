package com.jetbrains.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.model.DialogsEntity;
import com.jetbrains.model.UsersEntity;
import com.jetbrains.service.Dialog;
import com.jetbrains.service.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@RestController
public class DialogController {

    @RequestMapping(value = "/rest/dialogs", method = RequestMethod.GET)
    public String dialogs(HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UsersEntity user = (new User()).getUserBySession(request.getSession());
        Set<DialogsEntity> dialogs = (new Dialog()).getDialogs(user);
        return mapper.writeValueAsString(dialogs);
    }
}
