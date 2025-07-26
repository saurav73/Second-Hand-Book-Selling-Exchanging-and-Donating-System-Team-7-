package com.bookbridge.repository;

import com.bookbridge.model.Book;
import com.bookbridge.model.Message;
import com.bookbridge.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) ORDER BY m.createdAt")
    List<Message> findConversation(User user1, User user2);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) ORDER BY m.createdAt")
    Page<Message> findConversationPaged(User user1, User user2, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.receiver = ?1 AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = ?1 AND m.isRead = false")
    Long countUnreadMessages(User user);
    
    @Query("SELECT m FROM Message m WHERE m.book = ?1 ORDER BY m.createdAt")
    List<Message> findMessagesByBook(Book book);
    
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.sender = ?1 THEN m.receiver ELSE m.sender END " +
           "FROM Message m " +
           "WHERE m.sender = ?1 OR m.receiver = ?1")
    List<User> findChatPartners(User user);
    
    @Query("SELECT m FROM Message m WHERE m.sender = ?1 OR m.receiver = ?1 ORDER BY m.createdAt DESC")
    Page<Message> findUserMessages(User user, Pageable pageable);
}
