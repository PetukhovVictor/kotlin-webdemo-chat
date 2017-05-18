package com.jetbrains.dto;

import com.jetbrains.domain.UserEntity;

import java.sql.Timestamp;
import java.util.Set;

public class DialogDTO {
    private String title;
    private Timestamp lastUpdateDate;
    private int id;
    private Set<UserDTO> participants;

    public String getTitle() {
        return title;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public int getId() {
        return id;
    }

    public Set<UserDTO> getParticipants() {
        return participants;
    }
}
