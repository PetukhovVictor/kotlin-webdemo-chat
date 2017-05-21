package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAOImpl;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.DialogDTO;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.ResponseError;
import com.jetbrains.util.UrlUtils;
import com.jetbrains.web.errors.ResponseErrors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class DialogController {
    /**
     * Рест-метод для получения списков диалогов.
     */
    @RequestMapping(
            value = "/rest/dialogs",
            method = RequestMethod.GET,
            produces = { "application/json;charset=UTF-8" })
    public String getDialogs(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        List dialogs = new DialogDAOImpl().getDialogs(user.getId());
        return new ObjectMapper().writeValueAsString(dialogs);
    }

    /**
     * Рест-метод для осуществления поиска собеседников.
     */
    @RequestMapping(
            value = "/rest/dialogs/search",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogsSearch(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        String searchPhrase = request.getParameter("phrase");
        if (searchPhrase == null) {
            return new ResponseError(ResponseErrors.SEARCH_PHRASE_NOT_SPECIFIED, response).toString();
        }
        List<UserDTO> users = new UserDAOImpl().searchUserByNameOrEmailWithExclude(searchPhrase, user.getId());
        return new ObjectMapper().writeValueAsString(users);
    }

    /**
     * Рест-метод для создания диалога.
     * В случае, диалог между двумя пользователями уже есть, возвращается он.
     */
    @RequestMapping(
            value = "/rest/dialog/create",
            method = RequestMethod.POST,
            produces = { "application/json;charset=UTF-8" })
    public String dialogCreate(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        UserDAOImpl userDao = new UserDAOImpl();
        UserDTO user = userDao.getCurrentUser(request.getSession());
        if (user == null) {
            return new ResponseError(ResponseErrors.NOT_AUTHORIZED, response).toString();
        }
        Integer interlocutorId = UrlUtils.getIntegerParam(request, "interlocutorId");
        if (interlocutorId == null || user.getId().equals(interlocutorId) || !userDao.existUser(interlocutorId)) {
            return new ResponseError(ResponseErrors.INCORRECT_INTERLOCUTOR_ID, response).toString();
        }
        DialogDAOImpl dialogService = new DialogDAOImpl();
        DialogDTO dialog = dialogService.getDialogByParticipants(interlocutorId, user.getId());
        if (dialog == null) {
            dialog = dialogService.createDialog(user.getId(), Math.abs(interlocutorId));
        }
        return new ObjectMapper().writeValueAsString(dialog);
    }
}
