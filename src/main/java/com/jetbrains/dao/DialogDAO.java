package com.jetbrains.dao;

import com.jetbrains.dto.DialogDTO;

import java.util.List;

public interface DialogDAO {
    /**
     * Получение списка диалогов.
     *
     * @param userId ID пользователя, диалоги которого нужно получить.
     *
     * @return Список DTO-объектов диалогов.
     */
    List<DialogDTO> getDialogs(Integer userId);

    /**
     * Получение диалога по ID.
     *
     * @param dialogId ID диалога.
     *
     * @return DTO-объект диалога.
     */
    DialogDTO getDialogById(Integer dialogId);

    /**
     * Получение диалога по ID и ID собеседника.
     * Возвращается проекция диалога, содержащая в том числе информацию о собеседнике.
     *
     * @param dialogId ID диалога.
     * @param interlocutorId ID собеседника.
     *
     * @return DTO-объект диалога.
     */
    DialogDTO getDialogByIdAndInterlocutor(Integer dialogId, Integer interlocutorId);

    /**
     * Получение диалога между двумя пользователями.
     *
     * @param userId1 ID первого пользователя.
     * @param userId2 ID второго пользователя.
     *
     * @return DTO-объект диалога (null, если диалога не существует).
     */
    DialogDTO getDialogByParticipants(Integer userId1, Integer userId2);

    /**
     * Проверка существования диалога по ID.
     *
     * @param dialogId ID диалога.
     *
     * @return Флаг, показывающий, существует ли диалог с заданным ID.
     */
    boolean dialogExist(Integer dialogId);

    /**
     * Создание диалога между двумя пользователями.
     *
     * @param userId1 ID первого пользователя.
     * @param userId2 ID второго пользователя.
     *
     * @return DTO-объект созданного диалога.
     */
    DialogDTO createDialog(Integer userId1, Integer userId2);
}
