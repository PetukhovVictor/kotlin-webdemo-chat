package com.jetbrains.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "dialog_participants", schema = "kotlin_webdemo")
public class DialogParticipantEntity {
    private Integer id;
    private Integer dialogId;
    private Integer participantId;
    private Timestamp joinDate;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "dialog_id")
    public Integer getDialogId() {
        return dialogId;
    }

    public void setDialogId(Integer dialogId) {
        this.dialogId = dialogId;
    }

    @Basic
    @Column(name = "participant_id")
    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    @Basic
    @Column(name = "join_date")
    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogParticipantEntity that = (DialogParticipantEntity) o;

        if (id != that.id) return false;
        if (dialogId != that.dialogId) return false;
        if (participantId != that.participantId) return false;
        if (joinDate != null ? !joinDate.equals(that.joinDate) : that.joinDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        Integer result = id;
        result = 31 * result + dialogId;
        result = 31 * result + participantId;
        result = 31 * result + (joinDate != null ? joinDate.hashCode() : 0);
        return result;
    }
}
