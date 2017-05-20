package com.jetbrains.domain;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "dialog_messages", schema = "kotlin_webdemo", catalog = "")
public class DialogMessageEntity {
    private Integer id;
    private String message;
    private Timestamp date;
    private Integer dialogId;
    private UserEntity author;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Basic
    @Column(name = "date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogMessageEntity that = (DialogMessageEntity) o;

        if (id != that.id) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "dialog_id")
    public Integer getDialogId() {
        return dialogId;
    }

    public void setDialogId(Integer dialogId) {
        this.dialogId = dialogId;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    @ManyToOne
    @JoinColumn(name = "author_id")
    public UserEntity getAuthor() {
        return this.author;
    }
}
