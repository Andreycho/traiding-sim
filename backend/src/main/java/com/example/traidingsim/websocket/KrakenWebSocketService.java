package com.example.traidingsim.websocket;

import com.example.traidingsim.model.dto.CryptoPriceDTO;
import com.example.traidingsim.mapper.CryptoPriceMapper;
import com.example.traidingsim.model.dto.CryptoPricePayloadDTO;
import com.example.traidingsim.model.dto.SubscribeMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ClientEndpoint
@Service
@Slf4j
public class KrakenWebSocketService {

    @Value("${kraken.websocket.uri}")
    private String krakenWebSocketUri;

    private final ObjectMapper objectMapper;
    private final FrontendWebSocketService frontendWebSocketService;
    private final Map<String, Double> cryptoPrices = new ConcurrentHashMap<>();

    public KrakenWebSocketService(ObjectMapper objectMapper, FrontendWebSocketService frontendWebSocketService) {
        this.objectMapper = objectMapper;
        this.frontendWebSocketService = frontendWebSocketService;
    }

    @PostConstruct
    public void init() {
        connectToKrakenWebSocket();
    }

    private void connectToKrakenWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(krakenWebSocketUri));
            log.info("Connected to Kraken WebSocket");
        } catch (Exception e) {
            log.error("Error connecting to Kraken WebSocket: {}", e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        try {
            SubscribeMessage subscribeMessage = SubscribeMessage.createDefaultSubscription();
            String jsonMessage = objectMapper.writeValueAsString(subscribeMessage);
            session.getAsyncRemote().sendText(jsonMessage);
            log.info("Subscribed to Kraken ticker channel: {}", jsonMessage);
        } catch (Exception e) {
            log.error("Error sending subscription message", e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            log.debug("Received message: {}", message);

            JsonNode jsonNode = objectMapper.readTree(message);
            JsonNode dataNode = jsonNode.path("data");

            if (dataNode.isArray() && dataNode.size() > 0) {
                JsonNode firstData = dataNode.get(0);

                String symbol = firstData.path("symbol").asText(null);
                double lastPrice = firstData.path("last").asDouble(Double.NaN);

                if (symbol != null && !symbol.isEmpty() && !Double.isNaN(lastPrice)) {
                    CryptoPricePayloadDTO payload = new CryptoPricePayloadDTO();
                    payload.setSymbol(symbol);
                    payload.setLastPrice(lastPrice);

                    CryptoPriceDTO dto = CryptoPriceMapper.toDTO(payload);

                    frontendWebSocketService.broadcastCryptoPrice(dto);

                    cryptoPrices.put(symbol, lastPrice);
                    log.info("Updated price for {}: {}", symbol, lastPrice);
                }
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
        }
    }


    public Map<String, Double> getCryptoPrices() {
        return Collections.unmodifiableMap(cryptoPrices);
    }
}