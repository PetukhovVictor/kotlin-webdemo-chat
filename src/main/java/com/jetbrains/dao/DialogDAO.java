package com.jetbrains.dao;

import com.jetbrains.dto.DialogDTO;

import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dto.DialogMessageDTO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import java.sql.Timestamp;
import java.util.List;

public class DialogDAO {
    private Session session;
    private SessionFactory sessionsFactory;

    public DialogDAO() {
        this.sessionsFactory = new Configuration().configure().buildSessionFactory();
        this.session = this.sessionsFactory.openSession();
    }

    protected void finalize() {
        this.session.close();
        this.sessionsFactory.close();
    }

    public List<DialogDTO> getDialogs(UserEntity user) {
        ProjectionList projections = Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("title"), "title")
                .add(Projections.property("lastUpdateDate"), "lastUpdateDate")
                .add(Projections.property("participants.id"), "interlocutorId")
                .add(Projections.property("participants.name"), "interlocutorName")
                .add(Projections.property("participants.picture"), "interlocutorPicture");

        Criteria criteria = session.createCriteria(DialogEntity.class)
                .createAlias("participants", "participants")
                .setProjection(projections)
                .add(Restrictions.ne("participants.id", user.getId()))
                .setResultTransformer(Transformers.aliasToBean(DialogDTO.class))
                .addOrder(Order.asc("lastUpdateDate"));

        return (List<DialogDTO>) criteria.list();
    }

    public DialogEntity getDialogById(int dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class);
        dialogCriteria.add(Restrictions.eq("id", dialogId));
        return (DialogEntity) dialogCriteria.uniqueResult();
    }

    public List<DialogMessageDTO> getMessages(DialogEntity dialog) {
        ProjectionList projections = Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("message"), "message")
                .add(Projections.property("date"), "date")
                .add(Projections.property("author.id"), "authorId")
                .add(Projections.property("author.name"), "authorName")
                .add(Projections.property("author.picture"), "authorPicture");

        Criteria criteria = session.createCriteria(DialogMessageEntity.class)
                .createAlias("author", "author")
                .setProjection(projections)
                .add(Restrictions.eq("dialogId", dialog.getId()))
                .setResultTransformer(Transformers.aliasToBean(DialogMessageDTO.class))
                .addOrder(Order.asc("date"));

        return (List<DialogMessageDTO>) criteria.list();
    }

    public DialogMessageEntity addMessage(DialogEntity dialog, UserEntity user, String message) {
        this.session.beginTransaction();
        DialogMessageEntity messageEntity = new DialogMessageEntity();
        messageEntity.setDialogId(dialog.getId());
        messageEntity.setAuthor(user);
        messageEntity.setDate(new Timestamp(System.currentTimeMillis()));
        messageEntity.setMessage(message);
        this.session.save(messageEntity);
        this.session.getTransaction().commit();
        return messageEntity;
    }
}
