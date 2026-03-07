package com.microdevice.messages.dto;

public record MessageResponse(long id, String content, String author, String createdAt) {
}
