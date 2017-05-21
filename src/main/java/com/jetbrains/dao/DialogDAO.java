package com.jetbrains.dao;

import com.jetbrains.dto.DialogDTO;

import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dto.DialogMessageDTO;
import com.jetbrains.util.HibernateUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DialogDAO {
    private final static Integer DIALOGS_PER_PAGE = 10;
    private final static Integer MESSAGES_PER_PAGE = 10;

    private Session session;

    public DialogDAO() {
        this.session = HibernateUtils.getSession();
    }

    static private ProjectionList getDialogProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("title"), "title")
                .add(Projections.property("lastUpdateDate"), "lastUpdateDate")
                .add(Projections.property("participants.id"), "interlocutorId")
                .add(Projections.property("participants.name"), "interlocutorName")
                .add(Projections.property("participants.picture"), "interlocutorPicture");
    }

    static private ProjectionList getMessageProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("message"), "message")
                .add(Projections.property("date"), "date")
                .add(Projections.property("author.id"), "authorId")
                .add(Projections.property("author.name"), "authorName")
                .add(Projections.property("author.picture"), "authorPicture");
    }

    public List<DialogDTO> getDialogs(UserEntity user, Integer lastDialogId) {
        Criteria criteria = session.createCriteria(DialogEntity.class)
                .createAlias("participants", "participants")
                .setProjection(DialogDAO.getDialogProjections())
                .add(Restrictions.ne("participants.id", user.getId()))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class))
                .addOrder(Order.desc("lastUpdateDate"))
                .addOrder(Order.desc("id"))
                .setMaxResults(DialogDAO.DIALOGS_PER_PAGE);
        if (lastDialogId != 0) {
            criteria.add(Restrictions.lt("id", lastDialogId));
        }

        return (List<DialogDTO>) criteria.list();
    }

    public DialogEntity getDialogById(Integer dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class);
        dialogCriteria.add(Restrictions.eq("id", dialogId));
        return (DialogEntity) dialogCriteria.uniqueResult();
    }

    public DialogEntity getDialogByParticipants(UserEntity user1, UserEntity user2) {
        Query dialogCriteria = session.createQuery("select dialogId from DialogParticipantEntity dpe1 where " +
                "dpe1.dialogId IN (select dpe2.dialogId from DialogParticipantEntity dpe2 where dpe2.participantId = :user2)" +
                "and dpe1.participantId = :user1");
        dialogCriteria.setParameter("user1", user1.getId());
        dialogCriteria.setParameter("user2", user2.getId());
        Integer dialogId = (Integer)dialogCriteria.uniqueResult();
        return dialogId == null ? null : this.getDialogById(dialogId);
    }

    public DialogDTO getDialogDTO(DialogEntity dialog, UserEntity user) {
        Criteria criteria = session.createCriteria(DialogEntity.class)
                .createAlias("participants", "participants")
                .setProjection(DialogDAO.getDialogProjections())
                .add(Restrictions.ne("participants.id", user.getId()))
                .add(Restrictions.eq("id", dialog.getId()))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class));

        return (DialogDTO)criteria.uniqueResult();
    }

    public List<DialogMessageDTO> getMessages(DialogEntity dialog, Integer lastMessageId) {
        Criteria criteria = session.createCriteria(DialogMessageEntity.class)
                .createAlias("author", "author")
                .setProjection(DialogDAO.getMessageProjections())
                .add(Restrictions.eq("dialogId", dialog.getId()))
                .setResultTransformer(Transformers.aliasToBean(DialogMessageDTO.class))
                .addOrder(Order.desc("date"))
                .addOrder(Order.desc("id"))
                .setMaxResults(DialogDAO.MESSAGES_PER_PAGE);
        if (lastMessageId != 0) {
            criteria.add(Restrictions.lt("id", lastMessageId));
        }

        return (List<DialogMessageDTO>) criteria.list();
    }

    public DialogMessageDTO getMessageById(Integer messageId) {
        Criteria criteria = session.createCriteria(DialogMessageEntity.class)
                .createAlias("author", "author")
                .setProjection(DialogDAO.getMessageProjections())
                .add(Restrictions.eq("id", messageId))
                .setResultTransformer(Transformers.aliasToBean(DialogMessageDTO.class));

        return (DialogMessageDTO) criteria.uniqueResult();
    }

    public DialogEntity createDialog(Integer userId1, Integer userId2) {
        this.session.beginTransaction();
        UserEntity user1 = new UserDAO().getUserById(userId1);
        UserEntity user2 = new UserDAO().getUserById(userId2);
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
        return dialog;
    }

    public DialogMessageEntity addMessage(DialogEntity dialog, UserEntity user, String message) {
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
        return messageEntity;
    }
}
