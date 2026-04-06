package com.example.ddchathub.repository;

import com.example.ddchathub.entity.LineChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LineChannelRepository extends JpaRepository<LineChannel, UUID> {

    Optional<LineChannel> findByLineDestinationId(String lineDestinationId);

    // ใช้ตรวจสอบชื่อซ้ำก่อน create
    boolean existsByChannelName(String channelName);

    Optional<LineChannel> findByChannelName(String channelName);

}