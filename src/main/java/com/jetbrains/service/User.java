package com.jetbrains.service;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.model.UsersEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpSession;

public class User {
    private Session session;
    private SessionFactory sessionsFactory;

    public User() {
        this.sessionsFactory = new Configuration().configure().buildSessionFactory();
        this.session = this.sessionsFactory.openSession();
    }

    protected void finalize() {
        this.session.close();
        this.sessionsFactory.close();
    }

    private UsersEntity getUserByColumn(String uniqueColumn, Object value) {
        Criteria userCriteria = this.session.createCriteria(UsersEntity.class);
        userCriteria.add(Restrictions.eq(uniqueColumn, value));
        return (UsersEntity) userCriteria.uniqueResult();
    }

    public UsersEntity getUserByGid(String gid) {
        return this.getUserByColumn("gid", gid);
    }

    public UsersEntity getUserById(int id) {
        return this.getUserByColumn("id", id);
    }

    public UsersEntity getUserBySession(HttpSession session) {
        return (UsersEntity) session.getAttribute("user");
    }

    private boolean isLogged(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    public void logout(HttpSession session) {
        session.removeAttribute("user");
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

    public void signIn(UsersEntity user, HttpSession session) {
        if (!this.isLogged(session)) {
            session.setAttribute("user", user);
        }
    }

    public void sign(Userinfoplus userInfo, HttpSession session) {
        String gid = userInfo.getId();
        UsersEntity user = this.getUserByGid(gid);
        if (user == null) {
            this.signUp(userInfo);
            user = this.getUserByGid(gid);
        }
        this.signIn(user, session);
    }
}
