package com.example.ddchathub.repository;

import com.example.ddchathub.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // เช็กว่ามีลูกค้ารายนี้ในระบบแล้วหรือยัง (เช็กจากเบอร์โทร)
    boolean existsByPhoneNumber(String phoneNumber);

    // ค้นหาลูกค้าจากเบอร์โทร
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    // ค้นหาลูกค้าจาก LINE User ID
    Optional<Customer> findByLineUserId(String lineUserId);
    @Query("SELECT c FROM Customer c JOIN c.tags t WHERE t.name = :tagName")
    List<Customer> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.tags " +
            "LEFT JOIN FETCH c.lineChannel " +
            "ORDER BY c.createdAt DESC")
    List<Customer> findAllWithRelationships();

    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.tags " +
            "LEFT JOIN FETCH c.lineChannel lc " +
            "WHERE lc.id = :channelId " + // กรองตรงนี้!
            "ORDER BY c.createdAt DESC")
    List<Customer> findAllWithRelationshipsByChannelId(@Param("channelId") UUID channelId);
}