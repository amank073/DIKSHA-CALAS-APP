package com.diksha.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isAlert = false;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private boolean clearedBySender = false;

    @Column(nullable = false)
    private boolean clearedByReceiver = false;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Message() {}

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean alert) {
        isAlert = alert;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isClearedBySender() {
        return clearedBySender;
    }

    public void setClearedBySender(boolean clearedBySender) {
        this.clearedBySender = clearedBySender;
    }

    public boolean isClearedByReceiver() {
        return clearedByReceiver;
    }

    public void setClearedByReceiver(boolean clearedByReceiver) {
        this.clearedByReceiver = clearedByReceiver;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
