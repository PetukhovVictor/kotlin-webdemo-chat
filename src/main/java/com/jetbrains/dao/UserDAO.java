package com.jetbrains.dao;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.domain.UserEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpSession;

public class UserDAO {
    private Session session;
    private SessionFactory sessionsFactory;

    public UserDAO() {
        this.sessionsFactory = new Configuration().configure().buildSessionFactory();
        this.session = this.sessionsFactory.openSession();
    }

    protected void finalize() {
        this.session.close();
        this.sessionsFactory.close();
    }

    private UserEntity getUserByColumn(String uniqueColumn, Object value) {
        Criteria userCriteria = this.session.createCriteria(UserEntity.class);
        userCriteria.add(Restrictions.eq(uniqueColumn, value));
        return (UserEntity) userCriteria.uniqueResult();
    }

    public UserEntity getUserByGid(String gid) {
        return this.getUserByColumn("gid", gid);
    }

    public UserEntity getUserById(int id) {
        return this.getUserByColumn("id", id);
    }

    public UserEntity getUserBySession(HttpSession session) {
        return (UserEntity) session.getAttribute("user");
    }

    private boolean isLogged(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    public void logout(HttpSession session) {
        session.removeAttribute("user");
    }

    private void signUp(Userinfoplus userInfo) {
        this.session.beginTransaction();
        UserEntity user = new UserEntity();
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

    public void signIn(UserEntity user, HttpSession session) {
        if (!this.isLogged(session)) {
            session.setAttribute("user", user);
        }
    }

    public void sign(Userinfoplus userInfo, HttpSession session) {
        String gid = userInfo.getId();
        UserEntity user = this.getUserByGid(gid);
        if (user == null) {
            this.signUp(userInfo);
            user = this.getUserByGid(gid);
        }
        this.signIn(user, session);
    }
}
