package com.jetbrains.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetbrains.dao.DialogDAOImpl;
import com.jetbrains.dao.UserDAOImpl;
import com.jetbrains.dto.DialogDTO;
import com.jetbrains.dto.UserDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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
    public String dialogs(HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UserDTO user = new UserDAOImpl().getCurrentUser(request.getSession());
        List dialogs = new DialogDAOImpl().getDialogs(user.getId());
        return mapper.writeValueAsString(dialogs);
    }

    /**
     * Рест-метод для осуществления поиска собеседников.
     */
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

    /**
     * Рест-метод для создания диалога.
     * В случае, диалог между двумя пользователями уже есть, возвращает он.
     */
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
