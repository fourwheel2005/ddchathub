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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(UUID channelId) { // 💡 1. เพิ่มพารามิเตอร์รับ channelId
        List<Customer> customers;

        if (channelId != null) {
            customers = customerRepository.findAllWithRelationshipsByChannelId(channelId);
        } else {
            customers = customerRepository.findAllWithRelationships();
        }

        return customers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "เบอร์โทรศัพท์นี้มีอยู่ในระบบแล้ว");
        }

        Customer customer = Customer.builder()
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse addTagToCustomer(UUID customerId, UUID tagId) {
        Customer customer = getCustomerById(customerId);
        Tag tag = getTagById(tagId);

        customer.addTag(tag);
        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse removeTagFromCustomer(UUID customerId, UUID tagId) {
        Customer customer = getCustomerById(customerId);
        Tag tag = getTagById(tagId);

        customer.removeTag(tag);
        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(String tagName) {
        List<Customer> customers;
        if (tagName != null && !tagName.isBlank()) {
            customers = customerRepository.findByTagName(tagName); // ⚠️ อย่าลืมไปใส่ JOIN FETCH ในเมธอดนี้ด้วยนะครับ
        } else {
            customers = customerRepository.findAllWithRelationships();
        }

        return customers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportCustomersToCsv(String tagName) {
        List<Customer> customers;
        if (tagName != null && !tagName.isBlank()) {
            customers = customerRepository.findByTagName(tagName);
        } else {
            customers = customerRepository.findAllWithRelationships();
        }

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("\uFEFF");
        csvBuilder.append("FullName,PhoneNumber,LineUserId\n");

        for (Customer c : customers) {
            String name = c.getFullName() != null ? c.getFullName() : "";
            String phone = c.getPhoneNumber() != null ? c.getPhoneNumber() : "";
            String lineId = c.getLineUserId() != null ? c.getLineUserId() : "";

            csvBuilder.append(String.format("\"%s\",\"%s\",\"%s\"\n", name, phone, lineId));
        }

        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    // --- Helper Methods ---

    private Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลลูกค้า"));
    }

    private Tag getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบข้อมูลแท็ก"));
    }

    private CustomerResponse mapToResponse(Customer customer) {

        List<TagResponse> tagResponses = (customer.getTags() != null)
                ? customer.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColorCode()))
                .toList() // ใช้ toList() ตามแบบฉบับ Java 16+
                : List.of();

        String channelName = (customer.getLineChannel() != null) ? customer.getLineChannel().getChannelName() : "LINE OA";
        String channelColor = (customer.getLineChannel() != null) ? customer.getLineChannel().getColorCode() : "#10B981";
        UUID channelId = (customer.getLineChannel() != null) ? customer.getLineChannel().getId() : null;

        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getProfilePictureUrl(),
                null,
                customer.getPhoneNumber(),
                tagResponses,
                channelName,
                channelColor,
                channelId
        );
    }
}