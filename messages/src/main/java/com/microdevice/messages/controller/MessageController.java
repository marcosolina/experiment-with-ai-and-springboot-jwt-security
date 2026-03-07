package com.microdevice.messages.controller;

import com.microdevice.messages.dto.CreateMessageRequest;
import com.microdevice.messages.dto.MessageResponse;
import com.microdevice.messages.model.Message;
import com.microdevice.messages.service.JwtValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final JwtValidationService jwtValidationService;

    public MessageController(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;

        messages.add(new Message(idGenerator.incrementAndGet(), "Welcome to the messaging system!", "system", LocalDateTime.now()));
        messages.add(new Message(idGenerator.incrementAndGet(), "This is a sample message.", "admin", LocalDateTime.now()));
        messages.add(new Message(idGenerator.incrementAndGet(), "JWT security is working!", "system", LocalDateTime.now()));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> listMessages() {
        List<MessageResponse> response = messages.stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMessage(@PathVariable long id) {
        return messages.stream()
            .filter(m -> m.getId() == id)
            .findFirst()
            .map(m -> ResponseEntity.ok(toResponse(m)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@RequestBody CreateMessageRequest request,
                                                          Authentication authentication) {
        Message message = new Message(
            idGenerator.incrementAndGet(),
            request.content(),
            authentication.getName(),
            LocalDateTime.now()
        );
        messages.add(message);
        return ResponseEntity.ok(toResponse(message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable long id) {
        boolean removed = messages.removeIf(m -> m.getId() == id);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Message deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "supervisorConnected", jwtValidationService.isSupervisorAvailable()
        ));
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getContent(),
            message.getAuthor(),
            message.getCreatedAt().toString()
        );
    }
}
