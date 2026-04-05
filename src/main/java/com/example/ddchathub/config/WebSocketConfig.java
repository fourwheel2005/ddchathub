package com.example.ddchathub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // กำหนดชื่อท่อส่งข้อมูลขาออก (ให้หน้าเว็บมารอรับที่นี่)
        config.enableSimpleBroker("/topic");
        // กำหนดชื่อท่อรับข้อมูลขาเข้า (เวลาหน้าเว็บจะส่งข้อความมาหา Spring Boot)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // สร้างจุดเชื่อมต่อให้ React วิ่งเข้ามาต่อท่อ (อนุญาตพอร์ต 5173 ของ Vite)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173")
                .withSockJS();
    }
}