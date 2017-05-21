package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAO;
import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dao.UserDAO;
import com.jetbrains.dto.DialogDTO;
import com.jetbrains.dto.DialogMessageDTO;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class DialogController {

    private SimpMessagingTemplate template;

    @Autowired
    public DialogController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @RequestMapping(
            value = "/rest/dialogs",
            method = RequestMethod.GET,
            produces = { "application/json;charset=UTF-8" })
    public String dialogs(HttpServletRequest request) throws JsonProcessingException {
        Integer lastDialogId = Url.getIntegerParam(request, "lastDialogId");
        if (lastDialogId == null) {
            lastDialogId = 0;
        }

        ObjectMapper mapper = new ObjectMapper();
        UserEntity user = (new UserDAO()).getUserBySession(request.getSession());
        List dialogs = (new DialogDAO()).getDialogs(user, lastDialogId);
        return mapper.writeValueAsString(dialogs);
    }

    @RequestMapping(
            value = "/rest/dialogs/search",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogsSearch(HttpServletRequest request) throws JsonProcessingException {
        String searchPhrase = request.getParameter("phrase");
        if (searchPhrase == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<UserDTO> users = (new UserDAO()).search(searchPhrase);
        return mapper.writeValueAsString(users);
    }

    @RequestMapping(
            value = "/rest/dialog/messages",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogMessages(HttpServletRequest request) throws JsonProcessingException {
        Integer lastMessageId = Url.getIntegerParam(request, "lastMessageId");
        Integer dialogId = Url.getIntegerParam(request, "dialogId");
        if (dialogId == null) {
            return null;
        }
        if (lastMessageId == null) {
            lastMessageId = 0;
        }
        DialogDAO dialogService = new DialogDAO();
        DialogEntity dialog = dialogService.getDialogById(dialogId);
        if (dialog == null) {
            return null;
        }
        List<DialogMessageDTO> messages = dialogService.getMessages(dialog, lastMessageId);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(messages);
    }

    @RequestMapping(
            value = "/rest/dialog/message/send",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
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

        DialogMessageDTO messageDTO = (new DialogDAO()).getMessageById(newMessage.getId());
        this.template.convertAndSend("/messages/subscribe", messageDTO);
        return mapper.writeValueAsString(messageDTO);
    }

    @RequestMapping(
            value = "/rest/dialog/create",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogCreate(HttpServletRequest request) throws JsonProcessingException {
        Integer interlocutorId;
        try {
            interlocutorId = Integer.parseInt(request.getParameter("interlocutorId"));
        } catch (NumberFormatException e) {
            return null;
        }
        UserDAO userDao = new UserDAO();
        UserEntity user = userDao.getUserBySession(request.getSession());
        DialogDAO dialogService = new DialogDAO();

        DialogEntity dialogEntity = dialogService.getDialogByParticipants(userDao.getUserById(interlocutorId), user);
        if (dialogEntity == null) {
            dialogEntity = dialogService.createDialog(user.getId(), Math.abs(interlocutorId));
        }
        ObjectMapper mapper = new ObjectMapper();
        DialogDTO dialog = dialogService.getDialogDTO(dialogEntity, user);
        return mapper.writeValueAsString(dialog);
    }

    @SendTo("/messages/subscribe")
    private DialogMessageDTO dialogMessageReceive(DialogMessageDTO message) throws JsonProcessingException {
        return message;
    }
}
