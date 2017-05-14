package com.jetbrains.service;

import com.jetbrains.model.UsersEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Set;

public class Dialog {
    private Session session;

    public Dialog() {
        SessionFactory sessions = new Configuration().configure().buildSessionFactory();
        this.session = sessions.openSession();
    }

    public Set getDialogs(UsersEntity user) {
        return user.getDialogs();
    }
}
