package com.microdevice.messages.model;

import java.time.Instant;

/**
 * Immutable domain model representing a message stored in-memory.
 */
public class Message {

    /** Unique identifier for the message. */
    private final long id;

    /** Text content of the message. */
    private final String content;

    /** Username of the message author. */
    private final String author;

    /** Timestamp indicating when the message was created. */
    private final Instant createdAt;

    /**
     * Constructs a new message with the given attributes.
     *
     * @param id        the unique message identifier
     * @param content   the text content
     * @param author    the username of the author
     * @param createdAt the creation timestamp
     */
    public Message(long id, String content, String author, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    /**
     * Returns the message identifier.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the text content of the message.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the author's username.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation date-time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
