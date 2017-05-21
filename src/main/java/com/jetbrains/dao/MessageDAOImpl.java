package com.jetbrains.dao;

import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dto.DialogMessageDTO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.messaging.handler.annotation.SendTo;

import java.sql.Timestamp;
import java.util.List;

public class MessageDAOImpl extends DAO implements MessageDAO {
    /**
     * Количество сообщений в одной порции, отдаваемой клиенту.
     */
    private final static Integer MESSAGES_PER_PAGE = 30;

    /**
     * Получение правил Hibernate проекций DialogMessageEntity на DialogMessageDTO.
     * Используется в public-методах, для отдачи объекта данных вовне.
     *
     * @return ProjectionList список проекций.
     */
    static private ProjectionList getMessageProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("message"), "message")
                .add(Projections.property("date"), "date")
                .add(Projections.property("author.id"), "authorId")
                .add(Projections.property("author.name"), "authorName")
                .add(Projections.property("author.picture"), "authorPicture");
    }

    /**
     * Получение списка сообщений, ID которых меньше переданного.
     * TODO: Необходимо сделать завязку на дату добавления + ID, а не только на ID.
     *
     * @param dialogId ID диалога, сообщения которого необходимо получить.
     * @param lastMessageId ID сообщения, сообщения с ID меньше которого необходимо получить.
     *
     * @return Список DTO-объектов сообщений.
     */
    public List<DialogMessageDTO> getMessages(Integer dialogId, Integer lastMessageId) {
        Criteria criteria = session.createCriteria(DialogMessageEntity.class)
                .createAlias("author", "author")
                .setProjection(MessageDAOImpl.getMessageProjections())
                .add(Restrictions.eq("dialogId", dialogId))
                .setResultTransformer(Transformers.aliasToBean(DialogMessageDTO.class))
                .addOrder(Order.desc("date"))
                .addOrder(Order.desc("id"))
                .setMaxResults(MessageDAOImpl.MESSAGES_PER_PAGE);
        if (lastMessageId != 0) {
            criteria.add(Restrictions.lt("id", lastMessageId));
        }

        return (List<DialogMessageDTO>) criteria.list();
    }

    /**
     * Получение сообщения по ID.
     *
     * @param messageId ID сообщения.
     *
     * @return DTO-объект сообщения.
     */
    public DialogMessageDTO getMessageById(Integer messageId) {
        Criteria criteria = session.createCriteria(DialogMessageEntity.class)
                .createAlias("author", "author")
                .setProjection(MessageDAOImpl.getMessageProjections())
                .add(Restrictions.eq("id", messageId))
                .setResultTransformer(Transformers.aliasToBean(DialogMessageDTO.class));

        return (DialogMessageDTO) criteria.uniqueResult();
    }

    /**
     * Добавление сообщения в диалог.
     *
     * @param dialogId ID диалога, в который необходимо добавить сообщение.
     * @param userId ID пользователя, от имени которого добавляется сообщение.
     * @param message Текст добавляемого сообщения.
     *
     * @return DTO-объект добавленного сообщения.
     */
    public DialogMessageDTO addMessage(Integer dialogId, Integer userId, String message) {
        UserEntity user = new UserDAOImpl()._getUserById(userId);
        DialogEntity dialog = new DialogDAOImpl()._getDialogById(dialogId);

        this.session.beginTransaction();
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());

        DialogMessageEntity messageEntity = new DialogMessageEntity();
        messageEntity.setDialogId(dialog.getId());
        messageEntity.setAuthor(user);
        messageEntity.setDate(currentDate);
        messageEntity.setMessage(message);
        dialog.setLastUpdateDate(currentDate);

        this.session.save(messageEntity);
        this.session.save(dialog);
        this.session.getTransaction().commit();

        return this.getMessageById(messageEntity.getId());
    }

    /**
     * Получение нового сообщения заданного диалога.
     * Метод осуществляет рассылку всем подписчикам заданного диалога.
     *
     * @param message - DTO-объект добавленного сообщения.
     *
     * @return DTO-объект добавленного сообщения.
     */
    @SendTo("/chat/{dialogId}")
    private DialogMessageDTO dialogMessageReceive(DialogMessageDTO message) {
        return message;
    }
}
