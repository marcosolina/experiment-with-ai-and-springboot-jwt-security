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

/**
 * REST controller providing CRUD operations for messages and a health endpoint.
 *
 * <p>Messages are stored in an in-memory {@link CopyOnWriteArrayList} with an
 * {@link AtomicLong} ID generator. The list is pre-seeded with three sample messages
 * on construction.</p>
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    /** Thread-safe in-memory store of messages. */
    private final List<Message> messages = new CopyOnWriteArrayList<>();

    /** Atomic counter used to generate unique message IDs. */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Service used to check supervisor connectivity for the health endpoint. */
    private final JwtValidationService jwtValidationService;

    /**
     * Constructs the controller, injects the JWT validation service, and seeds
     * the message store with three initial messages.
     *
     * @param jwtValidationService the service for checking supervisor availability
     */
    public MessageController(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;

        messages.add(new Message(idGenerator.incrementAndGet(), "Welcome to the messaging system!", "system", LocalDateTime.now()));
        messages.add(new Message(idGenerator.incrementAndGet(), "This is a sample message.", "admin", LocalDateTime.now()));
        messages.add(new Message(idGenerator.incrementAndGet(), "JWT security is working!", "system", LocalDateTime.now()));
    }

    /**
     * Returns all messages.
     *
     * @return a 200 response containing the list of all messages
     */
    @GetMapping
    public ResponseEntity<List<MessageResponse>> listMessages() {
        List<MessageResponse> response = messages.stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single message by its ID.
     *
     * @param id the message identifier
     * @return a 200 response with the message, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessage(@PathVariable long id) {
        return messages.stream()
            .filter(m -> m.getId() == id)
            .findFirst()
            .map(m -> ResponseEntity.ok(toResponse(m)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new message authored by the authenticated user.
     *
     * @param request        the request body containing the message content
     * @param authentication the current authenticated principal
     * @return a 200 response with the newly created message
     */
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

    /**
     * Deletes a message by its ID.
     *
     * @param id the message identifier
     * @return a 200 response with a confirmation, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable long id) {
        boolean removed = messages.removeIf(m -> m.getId() == id);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Message deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns the health status of the service, including whether the supervisor
     * auth server is currently reachable.
     *
     * @return a 200 response with status and supervisor connectivity information
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "supervisorConnected", jwtValidationService.isSupervisorAvailable()
        ));
    }

    /**
     * Converts a {@link Message} domain object to a {@link MessageResponse} DTO.
     *
     * @param message the domain message
     * @return the corresponding response DTO
     */
    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getContent(),
            message.getAuthor(),
            message.getCreatedAt().toString()
        );
    }
}
