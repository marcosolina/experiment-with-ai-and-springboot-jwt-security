package com.microdevice.messages.dto;

/**
 * DTO record representing the request body for creating a new message.
 *
 * @param content the text content of the message to create
 */
public record CreateMessageRequest(String content) {
}
