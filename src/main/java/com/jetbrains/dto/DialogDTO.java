package com.jetbrains.dto;

import java.sql.Timestamp;

/**
 * DTO-модель диалога. Отдается на клиент.
 */
public class DialogDTO {
    /**
     * ID диалога.
     */
    private Integer id;

    /**
     * Заголовок диалога.
     */
    private String title;

    /**
     * Дата последнего обновления диалога.
     */
    private Timestamp lastUpdateDate;

    /**
     * ID собеседника.
     */
    private Integer interlocutorId;

    /**
     * Имя собеседника.
     */
    private String interlocutorName;

    /**
     * Аватар собеседника.
     */
    private String interlocutorPicture;

    public String getTitle() {
        return title;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public Integer getId() {
        return id;
    }

    public Integer getInterlocutorId() {
        return interlocutorId;
    }

    public String getInterlocutorName() {
        return interlocutorName;
    }

    public String getInterlocutorPicture() {
        return interlocutorPicture;
    }
}
