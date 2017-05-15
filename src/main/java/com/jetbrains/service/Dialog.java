package com.jetbrains.service;

import com.jetbrains.model.DialogMessagesEntity;
import com.jetbrains.model.DialogsEntity;
import com.jetbrains.model.UsersEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Set;

public class Dialog {
    private Session session;
    private SessionFactory sessionsFactory;

    public Dialog() {
        this.sessionsFactory = new Configuration().configure().buildSessionFactory();
        this.session = this.sessionsFactory.openSession();
    }

    protected void finalize() {
        this.session.close();
        this.sessionsFactory.close();
    }

    public Set<DialogsEntity> getDialogs(UsersEntity user) {
        return user.getDialogs();
    }

    public DialogsEntity getDialogById(int dialogId) {
        Criteria dialogCriteria = this.session.createCriteria(DialogsEntity.class);
        dialogCriteria.add(Restrictions.eq("id", dialogId));
        return (DialogsEntity) dialogCriteria.uniqueResult();
    }

    public Set<DialogMessagesEntity> getMessages(DialogsEntity dialog) {
        return dialog.getMessages();
    }
}
