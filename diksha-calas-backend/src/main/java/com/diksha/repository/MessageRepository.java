package com.diksha.repository;

import com.diksha.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :user1 AND m.receiver.id = :user2) OR (m.sender.id = :user2 AND m.receiver.id = :user1) ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1") Long user1, @Param("user2") Long user2);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.timestamp < :threshold")
    void deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
