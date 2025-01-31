package com.elice.aurasphere.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트에서 구독할 수 있는 주소 ("/topic/**")
        registry.enableSimpleBroker("/topic");

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결할 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS(); // SockJS 지원
    }
}

