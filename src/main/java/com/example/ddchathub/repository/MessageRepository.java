package com.example.ddchathub.repository;

import com.example.ddchathub.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);
    Optional<Message> findTopByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.customer.id = :customerId AND m.senderType = 'CUSTOMER' AND m.isRead = false")
    void markMessagesAsReadByCustomerId(@Param("customerId") UUID customerId);

    long countByCustomerIdAndSenderTypeAndIsReadFalse(UUID customerId, String senderType);
}