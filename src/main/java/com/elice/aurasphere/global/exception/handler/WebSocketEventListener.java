package com.elice.aurasphere.global.exception.handler;


import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketEventListener(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Long userId = getUserIdFromHeaders(headerAccessor);

        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
            System.out.println("✅ WebSocket 연결됨: 사용자 ID " + userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        userSessions.forEach((userId, sessions) -> {
            if (sessions.remove(sessionId) && sessions.isEmpty()) {
                userSessions.remove(userId);
                System.out.println("❌ 사용자 오프라인 처리: ID " + userId);
            }
        });
    }

    private Long getUserIdFromHeaders(StompHeaderAccessor headerAccessor) {
        String authToken = headerAccessor.getFirstNativeHeader("Authorization");
        if (authToken != null && authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(authToken);
        }
        return null;
    }
}