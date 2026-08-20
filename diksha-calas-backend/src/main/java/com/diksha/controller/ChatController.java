package com.diksha.controller;

import com.diksha.dto.ContactDto;
import com.diksha.dto.MessageDto;
import com.diksha.entity.User;
import com.diksha.repository.UserRepository;
import com.diksha.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    public ChatController(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> getContacts(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(messageService.getContacts(user));
    }

    @GetMapping("/messages/{contactId}")
    public ResponseEntity<List<MessageDto>> getConversation(Authentication auth, @PathVariable Long contactId) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(messageService.getConversation(user, contactId));
    }

    @PostMapping("/messages/{contactId}")
    public ResponseEntity<MessageDto> sendMessage(Authentication auth, @PathVariable Long contactId, @RequestBody Map<String, Object> payload) {
        User user = getCurrentUser(auth);
        String content = (String) payload.get("content");
        boolean isAlert = payload.containsKey("isAlert") && (Boolean) payload.get("isAlert");
        
        return ResponseEntity.ok(messageService.sendMessage(user, contactId, content, isAlert));
    }

    @DeleteMapping("/messages/{contactId}")
    public ResponseEntity<?> clearChat(Authentication auth, @PathVariable Long contactId) {
        User user = getCurrentUser(auth);
        messageService.clearChat(user, contactId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{contactId}/read")
    public ResponseEntity<?> markAsRead(Authentication auth, @PathVariable Long contactId) {
        User user = getCurrentUser(auth);
        messageService.markAsRead(user, contactId);
        return ResponseEntity.ok().build();
    }
}
