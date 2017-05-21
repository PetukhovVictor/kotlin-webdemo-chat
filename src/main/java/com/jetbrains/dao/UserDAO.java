package com.jetbrains.dao;

import com.google.api.services.oauth2.model.Userinfoplus;
import com.jetbrains.domain.UserEntity;
import com.jetbrains.dto.UserDTO;
import com.jetbrains.util.HibernateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;

import javax.servlet.http.HttpSession;
import java.util.List;

public class UserDAO {
    private Session session;

    static private ProjectionList getUserProjections() {
        return Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property("name"), "name")
                .add(Projections.property("picture"), "picture");
    }

    public UserDAO() {
        this.session = HibernateUtils.getSession();
    }

    private UserEntity getUserByColumn(String uniqueColumn, Object value) {
        Criteria userCriteria = this.session.createCriteria(UserEntity.class);
        userCriteria.add(Restrictions.eq(uniqueColumn, value));
        return (UserEntity) userCriteria.uniqueResult();
    }

    public UserEntity getUserByGid(String gid) {
        return this.getUserByColumn("gid", gid);
    }

    public UserEntity getUserById(Integer id) {
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

    public List<UserDTO> search(String searchPhrase) {
        Criteria criteria = session.createCriteria(UserEntity.class)
                .add(
                        Restrictions.or(
                                Restrictions.like("name", searchPhrase, MatchMode.ANYWHERE),
                                Restrictions.like("email", searchPhrase, MatchMode.EXACT)
                        )
                )
                .setProjection(UserDAO.getUserProjections())
                .setResultTransformer(Transformers.aliasToBean(UserDTO.class));

        return (List<UserDTO>) criteria.list();
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
