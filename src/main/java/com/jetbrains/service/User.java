package com.jetbrains.service;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.model.UsersEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class User {
    private Session session;

    public User() {
        SessionFactory sessions = new Configuration().configure().buildSessionFactory();
        this.session = sessions.openSession();
    }

    private UsersEntity getUser(String uniqueColumn, Object value) {
        Criteria userCriteria = this.session.createCriteria(UsersEntity.class);
        userCriteria.add(Restrictions.eq(uniqueColumn, value));
        return (UsersEntity) userCriteria.uniqueResult();
    }

    private UsersEntity getUserByGid(String gid) {
        return this.getUser("gid", gid);
    }

    private UsersEntity getUserById(int id) {
        return this.getUser("id", id);
    }

    private void signUp(Userinfoplus userInfo) {
        this.session.beginTransaction();
        UsersEntity user = new UsersEntity();
        user.setEmail(userInfo.getEmail());
        user.setGid(userInfo.getId());
        user.setName(userInfo.getName());
        String picture = userInfo.getPicture();
        if (picture != null) {
            user.setPicture(userInfo.getPicture());
        }
        this.session.save(user);
        this.session.getTransaction().commit();
    }

    private void signIn(UsersEntity user, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }

    public void sign(Userinfoplus userInfo, HttpServletRequest request) {
        String gid = userInfo.getId();
        UsersEntity user = this.getUserByGid(gid);
        if (user == null) {
            this.signUp(userInfo);
            user = this.getUserByGid(gid);
        }
        this.signIn(user, request);
    }
}
