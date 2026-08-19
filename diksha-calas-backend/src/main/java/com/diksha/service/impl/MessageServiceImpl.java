package com.diksha.service.impl;

import com.diksha.dto.ContactDto;
import com.diksha.dto.MessageDto;
import com.diksha.entity.Message;
import com.diksha.entity.StudentProfile;
import com.diksha.entity.User;
import com.diksha.enums.RoleType;
import com.diksha.repository.MessageRepository;
import com.diksha.repository.StudentProfileRepository;
import com.diksha.repository.UserRepository;
import com.diksha.service.MessageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, StudentProfileRepository studentProfileRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDto> getContacts(User user) {
        Set<User> contacts = new HashSet<>();
        
        if (RoleType.ADMIN.equals(user.getRole().getName())) {
            contacts.addAll(userRepository.findByRole_Name(RoleType.TEACHER));
            contacts.addAll(userRepository.findByRole_Name(RoleType.STUDENT));
        } else if (RoleType.TEACHER.equals(user.getRole().getName())) {
            contacts.addAll(userRepository.findByRole_Name(RoleType.ADMIN));
            List<StudentProfile> assignedProfiles = studentProfileRepository.findByAnyTeacherId(user.getId());
            for (StudentProfile profile : assignedProfiles) {
                contacts.add(profile.getUser());
            }
        } else if (RoleType.STUDENT.equals(user.getRole().getName())) {
            contacts.addAll(userRepository.findByRole_Name(RoleType.ADMIN));
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                if (profile.getPhysicsTeacher() != null) contacts.add(profile.getPhysicsTeacher());
                if (profile.getChemistryTeacher() != null) contacts.add(profile.getChemistryTeacher());
                if (profile.getMathsTeacher() != null) contacts.add(profile.getMathsTeacher());
                if (profile.getBiologyTeacher() != null) contacts.add(profile.getBiologyTeacher());
            }
        }

        return contacts.stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .map(u -> new ContactDto(u.getId(), u.getFirstName() + " " + u.getLastName(), u.getRole().getName().name(), u.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(User user, Long contactId) {
        List<Message> messages = messageRepository.findConversation(user.getId(), contactId);
        List<MessageDto> result = new ArrayList<>();
        
        for (Message m : messages) {
            boolean isSender = m.getSender().getId().equals(user.getId());
            boolean isReceiver = m.getReceiver().getId().equals(user.getId());
            
            if (isSender && m.isClearedBySender()) continue;
            if (isReceiver && m.isClearedByReceiver()) continue;
            
            MessageDto dto = new MessageDto();
            dto.setId(m.getId());
            dto.setSenderId(m.getSender().getId());
            dto.setSenderName(m.getSender().getFirstName() + " " + m.getSender().getLastName());
            dto.setSenderRole(m.getSender().getRole().getName().name());
            dto.setReceiverId(m.getReceiver().getId());
            dto.setReceiverName(m.getReceiver().getFirstName() + " " + m.getReceiver().getLastName());
            dto.setContent(m.getContent());
            dto.setAlert(m.isAlert());
            dto.setTimestamp(m.getTimestamp());
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional
    public MessageDto sendMessage(User sender, Long receiverId, String content, boolean isAlert) {
        if (RoleType.STUDENT.equals(sender.getRole().getName())) {
            // "Admin ka reply na kar paye only teacher ka reply kare"
            User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));
            if (RoleType.ADMIN.equals(receiver.getRole().getName())) {
                throw new RuntimeException("Students cannot reply to Admin");
            }
        }
        
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
                
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setAlert(isAlert);
        
        Message saved = messageRepository.save(message);
        
        MessageDto dto = new MessageDto();
        dto.setId(saved.getId());
        dto.setSenderId(saved.getSender().getId());
        dto.setSenderName(saved.getSender().getFirstName() + " " + saved.getSender().getLastName());
        dto.setSenderRole(saved.getSender().getRole().getName().name());
        dto.setReceiverId(saved.getReceiver().getId());
        dto.setReceiverName(saved.getReceiver().getFirstName() + " " + saved.getReceiver().getLastName());
        dto.setContent(saved.getContent());
        dto.setAlert(saved.isAlert());
        dto.setTimestamp(saved.getTimestamp());
        
        return dto;
    }

    @Override
    @Transactional
    public void clearChat(User user, Long contactId) {
        List<Message> messages = messageRepository.findConversation(user.getId(), contactId);
        for (Message m : messages) {
            if (m.getSender().getId().equals(user.getId())) {
                m.setClearedBySender(true);
            }
            if (m.getReceiver().getId().equals(user.getId())) {
                m.setClearedByReceiver(true);
            }
        }
        messageRepository.saveAll(messages);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2 AM
    public void cleanupOldMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(10);
        messageRepository.deleteOlderThan(threshold);
    }
}
