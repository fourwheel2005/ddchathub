package com.example.ddchathub.service;

import com.example.ddchathub.dto.CustomerRequest;
import com.example.ddchathub.dto.CustomerResponse;
import com.example.ddchathub.dto.TagResponse;
import com.example.ddchathub.entity.Customer;
import com.example.ddchathub.entity.Tag;
import com.example.ddchathub.repository.CustomerRepository;
import com.example.ddchathub.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TagRepository tagRepository; // ดึง TagRepository มาใช้ด้วย

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // เช็กเบอร์โทรซ้ำก่อนสร้างลูกค้าใหม่
        if (customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "เบอร์โทรศัพท์นี้มีอยู่ในระบบแล้ว");
        }

        Customer customer = Customer.builder()
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    // 💡 ฟีเจอร์หลัก: แอดมินกดติดแท็กให้ลูกค้า
    @Transactional
    public CustomerResponse addTagToCustomer(UUID customerId, UUID tagId) {
        Customer customer = getCustomerById(customerId);
        Tag tag = getTagById(tagId);

        customer.addTag(tag); // ใช้ Helper method ที่เราสร้างไว้ใน Entity
        return mapToResponse(customerRepository.save(customer));
    }

    // 💡 ฟีเจอร์หลัก: แอดมินกดลบแท็กออกจากลูกค้า
    @Transactional
    public CustomerResponse removeTagFromCustomer(UUID customerId, UUID tagId) {
        Customer customer = getCustomerById(customerId);
        Tag tag = getTagById(tagId);

        customer.removeTag(tag);
        return mapToResponse(customerRepository.save(customer));
    }

    // --- Helper Methods (ใช้ภายใน Service เพื่อความสะอาดของโค้ด) ---

    private Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลลูกค้า"));
    }

    private Tag getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลแท็ก"));
    }

    // แปลง Customer Entity ให้เป็น CustomerResponse DTO (พร้อมแนบ Tag ไปด้วย)
    private CustomerResponse mapToResponse(Customer customer) {
        // เผื่อกรณีลูกค้ายังไม่มีแท็ก จะได้ไม่เกิด NullPointerException
        Set<TagResponse> tagResponses = (customer.getTags() != null)
                ? customer.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColorCode()))
                .collect(Collectors.toSet())
                : Set.of();

        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                tagResponses
        );
    }



    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(String tagName) {
        List<Customer> customers;
        if (tagName != null && !tagName.isBlank()) {
            customers = customerRepository.findByTagName(tagName);
        } else {
            customers = customerRepository.findAll();
        }

        return customers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 2. 💡 สร้างฟังก์ชัน Export CSV
    @Transactional(readOnly = true)
    public byte[] exportCustomersToCsv(String tagName) {
        List<Customer> customers;
        if (tagName != null && !tagName.isBlank()) {
            customers = customerRepository.findByTagName(tagName);
        } else {
            customers = customerRepository.findAll();
        }

        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("\uFEFF");

        csvBuilder.append("FullName,PhoneNumber,LineUserId\n");

        for (Customer c : customers) {
            String name = c.getFullName() != null ? c.getFullName() : "";
            // FB Ads มักจะต้องการรหัสประเทศ แนะนำให้เผื่อจัดการเรื่อง +66 ไว้ในอนาคต
            String phone = c.getPhoneNumber() != null ? c.getPhoneNumber() : "";
            String lineId = c.getLineUserId() != null ? c.getLineUserId() : "";

            // เขียนข้อมูลแต่ละบรรทัด (ใช้เครื่องหมายคำพูดครอบเผื่อชื่อลูกค้ามีลูกน้ำ)
            csvBuilder.append(String.format("\"%s\",\"%s\",\"%s\"\n", name, phone, lineId));
        }

        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}