package com.example.traidingsim.websocket;

import com.example.traidingsim.model.dto.CryptoPriceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FrontendWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public FrontendWebSocketService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void broadcastCryptoPrice(CryptoPriceDTO priceDTO) {
        try {
            String message = objectMapper.writeValueAsString(priceDTO);
            messagingTemplate.convertAndSend("/topic/prices", message); // Use the STOMP broker
//            log.info("Broadcasted message to /topic/prices: {}", message);
        } catch (Exception e) {
            log.error("Error broadcasting message to WebSocket clients", e);
        }
    }
}