package com.jetbrains.dao;

import com.jetbrains.dto.DialogDTO;

import com.jetbrains.domain.DialogMessageEntity;
import com.jetbrains.domain.DialogEntity;
import com.jetbrains.domain.UserEntity;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

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

    public List getDialogs(UserEntity user) {
        ProjectionList projections = Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("title"), "title")
                .add(Projections.property("lastUpdateDate"), "lastUpdateDate")
                .add(Projections.groupProperty("pc.id"), "pc.id")
                .add(Projections.groupProperty("pc.name"), "pc.name")
                .add(Projections.groupProperty("pc.picture"), "pc.picture");

        Criteria cr = session.createCriteria(DialogEntity.class)
                .createAlias("participants",  "pc")
                .setProjection(projections)
                .setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

        List list = cr.list();
        return list;
    }

    public DialogEntity getDialogById(int dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogEntity.class);
        dialogCriteria.add(Restrictions.eq("id", dialogId));
        return (DialogEntity) dialogCriteria.uniqueResult();
    }

    public Set<DialogMessageEntity> getMessages(DialogEntity dialog) {
        return dialog.getMessages();
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
