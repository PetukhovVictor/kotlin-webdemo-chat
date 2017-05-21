package com.jetbrains.dto;

import java.sql.Timestamp;

/**
 * DTO-модель сообщения в диалоге. Отдается на клиент.
 */
public class DialogMessageDTO {
    /**
     * ID сообщения.
     */
    private Integer id;

    /**
     * Дата отправки сообщения.
     */
    private Timestamp date;

    /**
     * Текст сообщения.
     */
    private String message;

    /**
     * ID автора сообщения.
     */
    private Integer authorId;

    /**
     * Имя автора сообщения.
     */
    private String authorName;

    /**
     * Аватар автора сообщения.
     */
    private String authorPicture;

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getDate() {
        return date;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPicture() {
        return authorPicture;
    }
}
