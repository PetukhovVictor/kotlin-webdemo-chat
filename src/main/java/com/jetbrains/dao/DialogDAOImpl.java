package com.jetbrains.dao;

import com.jetbrains.dto.DialogDTO;

import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogDAOImpl extends DAO implements DialogDAO {

    /**
     * Получение правил Hibernate проекций DialogEntity на DialogDTO.
     * Используется в public-методах, для отдачи объекта данных вовне.
     *
     * @return ProjectionList список проекций.
     */
    static private ProjectionList getDialogProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("title"), "title")
                .add(Projections.property("lastUpdateDate"), "lastUpdateDate")
                .add(Projections.property("participants.id"), "interlocutorId")
                .add(Projections.property("participants.name"), "interlocutorName")
                .add(Projections.property("participants.picture"), "interlocutorPicture");
    }

    /**
     * Получение диалога по ID.
     * Внутренний метод, возвращающий объект доменной модели.
     *
     * @param dialogId ID диалога.
     *
     * @return Сущность "Диалог".
     */
    protected DialogEntity _getDialogById(Integer dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class);
        dialogCriteria.add(Restrictions.eq("id", dialogId));
        return (DialogEntity) dialogCriteria.uniqueResult();
    }

    /**
     * Получение списка диалогов.
     *
     * @param userId ID пользователя, диалоги которого нужно получить.
     *
     * @return Список DTO-объектов диалогов.
     */
    public List<DialogDTO> getDialogs(Integer userId) {
        Criteria criteria = session.createCriteria(DialogEntity.class)
                .createAlias("participants", "participants")
                .setProjection(DialogDAOImpl.getDialogProjections())
                .add(Restrictions.ne("participants.id", userId))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class))
                .addOrder(Order.desc("lastUpdateDate"))
                .addOrder(Order.desc("id"));

        return (List<DialogDTO>) criteria.list();
    }

    /**
     * Получение диалога по ID.
     *
     * @param dialogId ID диалога.
     *
     * @return DTO-объект диалога.
     */
    public DialogDTO getDialogById(Integer dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class)
                .setProjection(DialogDAOImpl.getDialogProjections())
                .createAlias("participants", "participants")
                .add(Restrictions.eq("id", dialogId))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class))
                .setMaxResults(1);

        return (DialogDTO) dialogCriteria.uniqueResult();
    }

    /**
     * Получение диалога по ID и ID собеседника.
     * Возвращается проекция диалога, содержащая в том числе информацию о собеседнике.
     *
     * @param dialogId ID диалога.
     * @param interlocutorId ID собеседника.
     *
     * @return DTO-объект диалога.
     */
    public DialogDTO getDialogByIdAndInterlocutor(Integer dialogId, Integer interlocutorId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class)
                .setProjection(DialogDAOImpl.getDialogProjections())
                .createAlias("participants", "participants")
                .add(Restrictions.eq("id", dialogId))
                .add(Restrictions.eq("participants.id", interlocutorId))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class));

        return (DialogDTO) dialogCriteria.uniqueResult();
    }

    /**
     * Получение диалога между двумя пользователями.
     *
     * @param userId1 ID первого пользователя.
     * @param userId2 ID второго пользователя.
     *
     * @return DTO-объект диалога (null, если диалога не существует).
     */
    public DialogDTO getDialogByParticipants(Integer userId1, Integer userId2) {
        Query dialogCriteria = session.createQuery("select dialogId from DialogParticipantEntity dpe1 where " +
                "dpe1.dialogId IN (select dpe2.dialogId from DialogParticipantEntity dpe2 where dpe2.participantId = :user2)" +
                "and dpe1.participantId = :user1");
        dialogCriteria.setParameter("user1", userId1);
        dialogCriteria.setParameter("user2", userId2);
        Integer dialogId = (Integer)dialogCriteria.uniqueResult();
        return dialogId == null ? null : this.getDialogByIdAndInterlocutor(dialogId, userId1);
    }

    /**
     * Проверка существования диалога по ID.
     *
     * @param dialogId ID диалога.
     *
     * @return Флаг, показывающий, существует ли диалог с заданным ID.
     */
    public boolean dialogExist(Integer dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class)
                .setProjection(DialogDAOImpl.getDialogProjections())
                .createAlias("participants", "participants")
                .add(Restrictions.eq("id", dialogId))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class));

        return !dialogCriteria.list().isEmpty();
    }

    /**
     * Создание диалога между двумя пользователями.
     *
     * @param userId1 ID первого пользователя.
     * @param userId2 ID второго пользователя.
     *
     * @return DTO-объект созданного диалога.
     */
    public DialogDTO createDialog(Integer userId1, Integer userId2) {
        this.session.beginTransaction();
        UserEntity user1 = new UserDAOImpl()._getUserById(userId1);
        UserEntity user2 = new UserDAOImpl()._getUserById(userId2);
        Set<UserEntity> participants = new HashSet<UserEntity>();
        participants.add(user1);
        participants.add(user2);

        Timestamp currentDate = new Timestamp(System.currentTimeMillis());

        DialogEntity dialog = new DialogEntity();
        dialog.setOwnerId(userId1);
        dialog.setParticipants(participants);
        dialog.setCreationDate(currentDate);
        dialog.setLastUpdateDate(currentDate);
        this.session.save(dialog);
        this.session.getTransaction().commit();
        return this.getDialogById(dialog.getId());
    }
}
