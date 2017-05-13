package com.jetbrains.service;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.model.UsersEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

public class User {
    private Session session;

    public User() {
        SessionFactory sessions = new Configuration().configure().buildSessionFactory();
        this.session = sessions.openSession();
    }

    public boolean isExist() {
        return false;
    }

    public boolean signUp(Userinfoplus userInfo) {

        return false;
    }

    public boolean signIn(int id) {
        return false;
    }

    public boolean sign(Userinfoplus userInfo) {
        Criteria userCriteria = this.session.createCriteria(UsersEntity.class);
        userCriteria.add(Restrictions.eq("gid", userInfo.getId()));
        UsersEntity user = (UsersEntity) userCriteria.uniqueResult();
        if (user == null) {
            this.signUp(userInfo);
        }
        this.signIn(user.getId());
        return false;
    }
}
