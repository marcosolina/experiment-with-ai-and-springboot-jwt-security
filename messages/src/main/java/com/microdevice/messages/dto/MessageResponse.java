package com.microdevice.messages.dto;

/**
 * DTO record returned as the API response for a message.
 *
 * @param id        the unique message identifier
 * @param content   the text content of the message
 * @param author    the username of the message author
 * @param createdAt the creation timestamp as an ISO-8601 string
 */
public record MessageResponse(long id, String content, String author, String createdAt) {
}
