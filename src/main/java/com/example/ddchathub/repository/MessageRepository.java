package com.example.ddchathub.repository;

import com.example.ddchathub.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);
    Optional<Message> findTopByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}