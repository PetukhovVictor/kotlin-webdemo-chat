package com.jetbrains.dao;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dto.UserDTO;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;

import javax.servlet.http.HttpSession;
import java.util.List;

public class UserDAOImpl extends DAO implements UserDAO {

    /**
     * Получение правил Hibernate проекций UserEntity на UserDTO.
     * Используется в public-методах, для отдачи объекта данных вовне.
     *
     * @return ProjectionList список проекций.
     */
    static private ProjectionList getUserProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("name"), "name")
                .add(Projections.property("picture"), "picture");
    }

    /**
     * Получение пользователя по значению уникального поля.
     * Внутренний метод, возвращающий объект доменной модели.
     *
     * @param uniqueField Название поля с уникальными значениями.
     * @param value Значение поля.
     *
     * @return Сущность "Пользователь".
     */
    private UserEntity _getUserByUniqueField(String uniqueField, Object value) {
        Criteria userCriteria = this.session.createCriteria(UserEntity.class);
        userCriteria.add(Restrictions.eq(uniqueField, value));
        return (UserEntity) userCriteria.uniqueResult();
    }

    /**
     * Получение пользователя по Google account ID.
     * Внутренний метод, возвращающий объект доменной модели.
     *
     * @param gid Google account ID.
     *
     * @return Сущность "Пользователь".
     */
    protected UserEntity _getUserByGid(String gid) {
        return this._getUserByUniqueField("gid", gid);
    }

    /**
     * Получение пользователя по ID.
     *
     * @param id Идентификатор пользователя.
     *
     * @return Сущность "Пользователь".
     */
    protected UserEntity _getUserById(Integer id) {
        return this._getUserByUniqueField("id", id);
    }

    /**
     * Осуществление регистрации.
     *
     * @param userInfo Информация о пользователя (в формате Google account).
     */
    protected UserEntity _signUp(Userinfoplus userInfo) {
        this.session.beginTransaction();
        UserEntity user = new UserEntity();
        user.setEmail(userInfo.getEmail());
        user.setGid(userInfo.getId());
        user.setName(userInfo.getName());
        String picture = userInfo.getPicture();
        if (picture != null) {
            user.setPicture(userInfo.getPicture());
        }
        this.session.save(user);
        this.session.getTransaction().commit();
        return user;
    }

    /**
     * Осуществление аутентификации и авторизации.
     *
     * @param user Сущность "Пользователь".
     * @param session Обьект сессии.
     */
    protected void _signIn(UserEntity user, HttpSession session) {
        if (!this.isLogged(session)) {
            session.setAttribute("user", user);
        }
    }

    /**
     * Проверка, существует ли пользователь.
     *
     * @param userId ID пользователя.
     *
     * @return Флаг, показывающий, существует ли пользователь с переданным ID.
     */
    public boolean existUser(Integer userId) {
        return this._getUserByUniqueField("id", userId) != null;
    }

    /**
     * Получение текущего пользователя (по сессии).
     *
     * @param session Обьект сессии.
     *
     * @return DTO-объект "Пользователь".
     */
    public UserDTO getCurrentUser(HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        Criteria criteria = this.session.createCriteria(UserEntity.class)
                .add(Restrictions.eq("id", user.getId()))
                .setProjection(UserDAOImpl.getUserProjections())
                .setResultTransformer(Transformers.aliasToBean(UserDTO.class));
        return (UserDTO)criteria.uniqueResult();
    }

    /**
     * Проверка, авторизован ли пользователь.
     *
     * @param session Обьект сессии.
     *
     * @return Результат проверки на авторизованность.
     */
    public boolean isLogged(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    /**
     * Осуществление выхода пользователя из системы (очистки сессии).
     *
     * @param session Обьект сессии.
     */
    public void logout(HttpSession session) {
        session.removeAttribute("user");
    }

    /**
     * Поиск пользователей по имени или по e-mail с исключением заданного пользователя.
     * Поиск по имени происходит по подстроке, по e-mail - по точному совпадению.
     *
     * @param searchPhrase Поисковая фраза.
     *
     * @return Список DTO-объектов "Пользователь".
     */
    public List<UserDTO> searchUserByNameOrEmailWithExclude(String searchPhrase, Integer excludedUserId) {
        Criteria criteria = session.createCriteria(UserEntity.class)
                .add(
                        Restrictions.or(
                                Restrictions.like("name", searchPhrase, MatchMode.ANYWHERE),
                                Restrictions.like("email", searchPhrase, MatchMode.EXACT)
                        )
                )
                .add(Restrictions.ne("id", excludedUserId))
                .setProjection(UserDAOImpl.getUserProjections())
                .setResultTransformer(Transformers.aliasToBean(UserDTO.class));

        return (List<UserDTO>) criteria.list();
    }

    /**
     * Вход пользователя в систему.
     * Если пользователь не зарегистрирован, происходит предварительная регистрации.
     *
     * @param userInfo Информация о пользователя (в формате Google account).
     * @param session Обьект сессии.
     */
    public void sign(Userinfoplus userInfo, HttpSession session) {
        String gid = userInfo.getId();
        UserEntity user = this._getUserByGid(gid);
        if (user == null) {
            user = this._signUp(userInfo);
        }
        this._signIn(user, session);
    }
}
