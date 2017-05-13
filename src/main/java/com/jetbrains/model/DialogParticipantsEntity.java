package com.jetbrains.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "dialog_participants", schema = "kotlin_webdemo", catalog = "")
public class DialogParticipantsEntity {
    private int id;
    private Timestamp joinDate;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

        DialogParticipantsEntity that = (DialogParticipantsEntity) o;

        if (id != that.id) return false;
        if (joinDate != null ? !joinDate.equals(that.joinDate) : that.joinDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (joinDate != null ? joinDate.hashCode() : 0);
        return result;
    }
}
