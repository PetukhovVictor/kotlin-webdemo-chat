package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAOImpl;
import com.jetbrains.dao.MessageDAOImpl;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.DialogMessageDTO;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.ResponseError;
import com.jetbrains.util.UrlUtils;
import com.jetbrains.web.errors.ResponseErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public String dialogMessages(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        Integer lastMessageId = UrlUtils.getIntegerParam(request, "lastMessageId");
        Integer dialogId = UrlUtils.getIntegerParam(request, "dialogId");
        if (dialogId == null) {
            return new ResponseError(ResponseErrors.INCORRECT_DIALOG_ID, response).toString();
        }
        DialogDAOImpl dialogDao = new DialogDAOImpl();
        if (!dialogDao.dialogExist(dialogId) || !dialogDao.dialogCheckAccess(dialogId, user.getId())) {
            return new ResponseError(ResponseErrors.DIALOG_DOES_NOT_EXIST, response).toString();
        }
        List<DialogMessageDTO> messages = new MessageDAOImpl().getMessages(dialogId, lastMessageId != null ? lastMessageId : 0);
        return new ObjectMapper().writeValueAsString(messages);
    }

    /**
     * Рест-метод для отправки сообщения.
     * После успешной записи сообщения в базу осуществляется рассылка информации о новом сообщений всем подписчикам.
     */
    @RequestMapping(
            value = "/rest/dialog/message/send",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogMessageSend(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        Integer dialogId = UrlUtils.getIntegerParam(request, "dialogId");
        if (dialogId == null) {
            return new ResponseError(ResponseErrors.INCORRECT_DIALOG_ID, response).toString();
        }
        String message = request.getParameter("message");
        if (message == null) {
            return new ResponseError(ResponseErrors.MESSAGE_NOT_SPECIFIED, response).toString();
        }
        DialogDAOImpl dialogDao = new DialogDAOImpl();
        if (!dialogDao.dialogExist(dialogId) || !dialogDao.dialogCheckAccess(dialogId, user.getId())) {
            return new ResponseError(ResponseErrors.DIALOG_DOES_NOT_EXIST, response).toString();
        }
        DialogMessageDTO newMessage = new MessageDAOImpl().addMessage(dialogId, user.getId(), message);
        this.template.convertAndSend("/chat/" + dialogId, newMessage);
        return new ObjectMapper().writeValueAsString(newMessage);
    }
}
