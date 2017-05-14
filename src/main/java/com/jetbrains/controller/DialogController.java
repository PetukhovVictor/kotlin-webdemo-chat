package com.jetbrains.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.model.DialogMessagesEntity;
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

    @RequestMapping(value = "/rest/dialog/messages", method = RequestMethod.POST)
    public String dialogMessages(HttpServletRequest request) throws JsonProcessingException {
        String dialogIdString = request.getParameter("dialogId");
        if (dialogIdString == null) {
            return null;
        }
        int dialogId;
        try {
            dialogId = Integer.parseInt(dialogIdString);
        } catch (NumberFormatException e) {
            return null;
        }
        Dialog dialogService = new Dialog();
        DialogsEntity dialog = dialogService.getDialogById(dialogId);
        Set<DialogMessagesEntity> messages = dialogService.getMessages(dialog);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(messages);
    }
}
