package com.example.ddchathub.repository;

import com.example.ddchathub.entity.LineChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LineChannelRepository extends JpaRepository<LineChannel, UUID> {

    // 💡 Method นี้สำคัญมาก: ใช้สำหรับค้นหา Channel จาก Destination ID ที่ LINE Webhook ส่งมา
    Optional<LineChannel> findByLineDestinationId(String lineDestinationId);

}