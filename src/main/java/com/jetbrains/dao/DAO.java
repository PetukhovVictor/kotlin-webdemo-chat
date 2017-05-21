package com.jetbrains.dao;

import com.jetbrains.util.HibernateUtils;
import org.hibernate.Session;

public abstract class DAO {
    protected Session session;

    protected DAO() {
        this.session = HibernateUtils.getSession();
    }
}
