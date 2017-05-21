package com.jetbrains.dao;

import com.jetbrains.dto.DialogMessageDTO;

import java.util.List;

public interface MessageDAO {
    /**
     * Получение списка сообщений, ID которых меньше переданного.
     * TODO: Необходимо сделать завязку на дату добавления + ID, а не только на ID.
     *
     * @param dialogId ID диалога, сообщения которого необходимо получить.
     * @param lastMessageId ID сообщения, сообщения с ID меньше которого необходимо получить.
     *
     * @return Список DTO-объектов сообщений.
     */
    List<DialogMessageDTO> getMessages(Integer dialogId, Integer lastMessageId);

    /**
     * Получение сообщения по ID.
     *
     * @param messageId ID сообщения.
     *
     * @return DTO-объект сообщения.
     */
    DialogMessageDTO getMessageById(Integer messageId);

    /**
     * Добавление сообщения в диалог.
     *
     * @param dialogId ID диалога, в который необходимо добавить сообщение.
     * @param userId ID пользователя, от имени которого добавляется сообщение.
     * @param message Текст добавляемого сообщения.
     *
     * @return DTO-объект добавленного сообщения.
     */
    DialogMessageDTO addMessage(Integer dialogId, Integer userId, String message);
}
