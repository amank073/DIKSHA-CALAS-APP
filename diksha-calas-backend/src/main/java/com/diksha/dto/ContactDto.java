package com.diksha.dto;

public class ContactDto {
    private Long id;
    private String name;
    private String role;
    private String email;
    private int unreadCount;
    private java.time.LocalDateTime lastMessageTime;

    public ContactDto() {}

    public ContactDto(Long id, String name, String role, String email, int unreadCount) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.unreadCount = unreadCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public java.time.LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(java.time.LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
