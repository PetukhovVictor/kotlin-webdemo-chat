package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAOImpl;
import com.jetbrains.dao.MessageDAOImpl;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.DialogMessageDTO;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class MessageController {
    /**
     * Объект, предоставляющий функционал внутреннего взаимодействия с брокером сообщений (для работы с веб-сокетами).
     */
    private SimpMessagingTemplate template;

    @Autowired
    public MessageController(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * Рест-метод для получения списка сообщений диалога.
     */
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

    /**
     * Рест-метод для отправки сообщения.
     * После успешной записи сообщения в базу осуществляется рассылка информации о новом сообщений всем подписчикам.
     */
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
}
