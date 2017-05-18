package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAO;
import com.jetbrains.dto.DialogDTO;
import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dao.UserDAO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
public class DialogController {

    @RequestMapping(value = "/rest/dialogs", method = RequestMethod.GET)
    public String dialogs(HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserEntity user = (new UserDAO()).getUserBySession(request.getSession());
        List dialogs = (new DialogDAO()).getDialogs(user);
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
        DialogDAO dialogService = new DialogDAO();
        DialogEntity dialog = dialogService.getDialogById(dialogId);
        Set<DialogMessageEntity> messages = dialogService.getMessages(dialog);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(messages);
    }

    @RequestMapping(value = "/rest/dialog/message/send", method = RequestMethod.POST)
    public String dialogMessageSend(HttpServletRequest request) throws JsonProcessingException {
        String dialogIdString = request.getParameter("dialogId");
        String message = request.getParameter("message");
        UserEntity user = (new UserDAO()).getUserBySession(request.getSession());
        if (dialogIdString == null || message == null) {
            return null;
        }
        int dialogId;
        try {
            dialogId = Integer.parseInt(dialogIdString);
        } catch (NumberFormatException e) {
            return null;
        }
        DialogDAO dialogService = new DialogDAO();
        DialogMessageEntity newMessage = dialogService.addMessage(dialogService.getDialogById(dialogId), user, message);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(newMessage);
    }
}
