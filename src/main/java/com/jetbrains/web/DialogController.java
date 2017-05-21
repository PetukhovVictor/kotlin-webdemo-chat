package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAOImpl;
import com.jetbrains.dao.MessageDAOImpl;
import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dao.UserDAOImpl;
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
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        List dialogs = new DialogDAOImpl().getDialogs(user.getId());
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
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        List<UserDTO> users = new UserDAOImpl().searchUserByNameOrEmailWithExclude(searchPhrase, user.getId());
        ObjectMapper mapper = new ObjectMapper();
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
        DialogDAOImpl dialogService = new DialogDAOImpl();
        if (!dialogService.dialogExist(dialogId)) {
            return null;
        }
        List<DialogMessageDTO> messages = new MessageDAOImpl().getMessages(dialogId, lastMessageId);
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
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (dialogIdString == null || message == null) {
            return null;
        }
        Integer dialogId;
        try {
            dialogId = Integer.parseInt(dialogIdString);
        } catch (NumberFormatException e) {
            return null;
        }
        DialogMessageDTO newMessage = new MessageDAOImpl().addMessage(dialogId, user.getId(), message);

        ObjectMapper mapper = new ObjectMapper();
        this.template.convertAndSend("/chat/" + dialogId, newMessage);
        return mapper.writeValueAsString(newMessage);
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
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        DialogDAOImpl dialogService = new DialogDAOImpl();

        DialogDTO dialog = dialogService.getDialogByParticipants(interlocutorId, user.getId());
        if (dialog == null) {
            dialog = dialogService.createDialog(user.getId(), Math.abs(interlocutorId));
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(dialog);
    }
}
