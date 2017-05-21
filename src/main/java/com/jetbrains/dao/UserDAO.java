package com.jetbrains.dao;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.dto.UserDTO;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface UserDAO {
    /**
     * Проверка, существует ли пользователь.
     *
     * @param userId ID пользователя.
     *
     * @return Флаг, показывающий, существует ли пользователь с переданным ID.
     */
    boolean existUser(Integer userId);

    /**
     * Получение текущего пользователя (по сессии).
     *
     * @param session Обьект сессии.
     *
     * @return DTO-объект "Пользователь".
     */
    UserDTO getCurrentUser(HttpSession session);

    /**
     * Проверка, авторизован ли пользователь.
     *
     * @param session Обьект сессии.
     *
     * @return Результат проверки на авторизованность.
     */
    boolean isLogged(HttpSession session);

    /**
     * Осуществление выхода пользователя из системы (очистки сессии).
     *
     * @param session Обьект сессии.
     */
    void logout(HttpSession session);

    /**
     * Поиск пользователей по имени или по e-mail с исключением заданного пользователя.
     * Поиск по имени происходит по подстроке, по e-mail - по точному совпадению.
     *
     * @param searchPhrase Поисковая фраза.
     *
     * @return Список DTO-объектов "Пользователь".
     */
    List<UserDTO> searchUserByNameOrEmailWithExclude(String searchPhrase, Integer excludedUserId);

    /**
     * Вход пользователя в систему.
     * Если пользователь не зарегистрирован, происходит предварительная регистрации.
     *
     * @param userInfo Информация о пользователя (в формате Google account).
     * @param session Обьект сессии.
     */
    void sign(Userinfoplus userInfo, HttpSession session);
}
