package com.example.ddchathub.controller;

import com.example.ddchathub.dto.CustomerRequest;
import com.example.ddchathub.dto.CustomerResponse;
import com.example.ddchathub.service.CustomerService;
import com.linecorp.bot.messaging.model.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;



    // สร้างลูกค้าใหม่
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
    }

    @PostMapping("/{customerId}/tags/{tagId}")
    public ResponseEntity<CustomerResponse> addTagToCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(customerService.addTagToCustomer(customerId, tagId));
    }

    @DeleteMapping("/{customerId}/tags/{tagId}")
    public ResponseEntity<CustomerResponse> removeTagFromCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return ResponseEntity.ok(customerService.removeTagFromCustomer(customerId, tagId));
    }





    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String tag) { // required = false หมายถึงไม่ส่งแท็กมาก็ได้ (ดึงทั้งหมด)
        return ResponseEntity.ok(customerService.getCustomers(tag));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomersCsv(
            @RequestParam(required = false) String tag) {

        byte[] csvData = customerService.exportCustomersToCsv(tag);

        String fileName = (tag != null ? tag : "all_customers") + "_export.csv";

        HttpHeaders headers = new HttpHeaders();
        // สั่งให้ Browser ดาวน์โหลดไฟล์แทนการแสดงผล
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}