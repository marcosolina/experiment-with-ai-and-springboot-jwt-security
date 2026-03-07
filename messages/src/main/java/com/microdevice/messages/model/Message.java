package com.microdevice.messages.model;

import java.time.LocalDateTime;

public class Message {

    private final long id;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public Message(long id, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
