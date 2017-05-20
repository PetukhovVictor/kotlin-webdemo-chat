package com.jetbrains.dto;

import java.sql.Timestamp;

public class DialogMessageDTO {
    private Integer id;
    private Timestamp date;
    private String message;
    private Integer authorId;
    private String authorName;
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
