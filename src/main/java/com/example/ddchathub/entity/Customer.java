package com.example.ddchathub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "line_user_id", unique = true)
    private String lineUserId;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_channel_id") // ชื่อคอลัมน์ใน Database ที่ใช้เชื่อมกัน
    private LineChannel lineChannel;


    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "customer_tags",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // จัดการเวลาแก้ไขอัตโนมัติ
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // เพิ่มฟิลด์นี้เข้าไปครับ (เก็บชื่อแอดมิน)
    @Column(name = "assigned_admin")
    private String assignedAdmin;

    // Helper method สำหรับเพิ่ม/ลบ Tag ให้เขียนโค้ดง่ายขึ้นใน Service
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }
}