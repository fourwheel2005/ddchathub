package com.example.ddchathub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 💡 เปลี่ยนตรงนี้
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "color_code")
    private String colorCode;
}