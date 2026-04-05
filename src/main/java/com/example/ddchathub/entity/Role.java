package com.example.ddchathub.entity;

public enum Role {
    SUPER_ADMIN, // เจ้าของระบบ ทำได้ทุกอย่าง (เพิ่มแอดมินคนอื่นได้)
    ADMIN,       // ผู้จัดการ จัดการลูกค้าและแท็กได้
    STAFF        // พนักงาน ตอบแชทได้อย่างเดียว ลบอะไรไม่ได้
}