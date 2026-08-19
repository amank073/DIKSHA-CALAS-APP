package com.diksha.service;

import com.diksha.dto.ContactDto;
import com.diksha.dto.MessageDto;
import com.diksha.entity.User;

import java.util.List;

public interface MessageService {
    List<ContactDto> getContacts(User user);
    List<MessageDto> getConversation(User user, Long contactId);
    MessageDto sendMessage(User sender, Long receiverId, String content, boolean isAlert);
    void clearChat(User user, Long contactId);
    void cleanupOldMessages();
}
