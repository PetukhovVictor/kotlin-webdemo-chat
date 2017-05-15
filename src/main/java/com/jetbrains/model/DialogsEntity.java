package com.jetbrains.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "dialogs", schema = "kotlin_webdemo", catalog = "")
public class DialogsEntity {
    private int id;
    private Timestamp creationDate;
    private String title;
    private Timestamp lastUpdateDate;
    private int ownerId;
    private Set<UsersEntity> participants;
    private Set<DialogMessagesEntity> messages;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "creation_date")
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "last_update_date")
    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogsEntity that = (DialogsEntity) o;

        if (id != that.id) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (lastUpdateDate != null ? !lastUpdateDate.equals(that.lastUpdateDate) : that.lastUpdateDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (lastUpdateDate != null ? lastUpdateDate.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "owner_id")
    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setParticipants(Set<UsersEntity> participants) {
        this.participants = participants;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "dialog_participants",
            joinColumns = @JoinColumn(name = "dialog_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    public Set<UsersEntity> getParticipants() {
        return this.participants;
    }

    public void setMessages(Set<DialogMessagesEntity> messages) {
        this.messages = messages;
    }

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dialogId")
    public Set<DialogMessagesEntity> getMessages() {
        return this.messages;
    }
}
