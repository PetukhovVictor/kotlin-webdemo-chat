package com.jetbrains.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Вспомогательный класс для работы с сессиями Hibernate.
 * TODO: Необходимо происледовать жизненный цикл сессий и фабрики сессий и проанализировать корректность и исключительные ситации данной схемы получения сессии.
 */
public class HibernateUtils {
    /**
     * Фабрика сессий. Конструируется и устанавливается единожды.
     */
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Сессия. Создается по первому запросу.
     */
    private static Session session = null;

    /**
     * Конфигурирование и создание фабрики сессий.
     *
     * @return Фабрика сессий.
     */
    private static SessionFactory buildSessionFactory() {
        return new Configuration().configure().buildSessionFactory();
    }

    /**
     * Получение текущей Hibernate сессии.
     * При первом обращение происходит создание сессии.
     *
     * @return Hibernate-сессия.
     */
    public static Session getSession() {
        if (session == null) {
            session = sessionFactory.openSession();
        }
        return session;
    }
}