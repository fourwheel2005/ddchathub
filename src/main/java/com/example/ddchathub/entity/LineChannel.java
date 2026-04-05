package com.example.ddchathub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Data
@Table(name = "line_channel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String channelName; // เช่น "สาขาปากเกร็ด", "สาขาหลัก"
    private String lineDestinationId; // คือรหัส destination ที่ได้จาก Webhook
    private String channelAccessToken;
    private String channelSecret;
}