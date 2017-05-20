package com.jetbrains.dto;

import java.sql.Timestamp;

public class DialogDTO {
    private String title;
    private Timestamp lastUpdateDate;
    private int id;
    private int interlocutorId;
    private String interlocutorName;
    private String interlocutorPicture;

    public String getTitle() {
        return title;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getId() {
        return id;
    }

    public int getInterlocutorId() {
        return interlocutorId;
    }

    public String getInterlocutorName() {
        return interlocutorName;
    }

    public String getInterlocutorPicture() {
        return interlocutorPicture;
    }
}
