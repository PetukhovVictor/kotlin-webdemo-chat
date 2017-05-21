package com.jetbrains.dto;

import java.sql.Timestamp;

public class DialogDTO {
    private String title;
    private Timestamp lastUpdateDate;
    private Integer id;
    private Integer interlocutorId;
    private String interlocutorName;
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
