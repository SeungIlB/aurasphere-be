package com.elice.aurasphere.global.exception.handler;


import com.elice.aurasphere.global.authentication.JwtTokenProvider;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketEventListener(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Long userId = getUserIdFromHeaders(headerAccessor); // 사용자 ID 가져오기

        if (userId != null) {
            sessionUserMap.put(sessionId, userId);
            System.out.println("✅ WebSocket 연결됨: 사용자 ID " + userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionUserMap.containsKey(sessionId)) {
            Long userId = sessionUserMap.remove(sessionId);
            System.out.println("❌ WebSocket 연결 종료: 사용자 ID " + userId);
        }
    }


    private Long getUserIdFromHeaders(StompHeaderAccessor headerAccessor) {
        // 1. 헤더에서 Authorization 토큰 가져오기
        String authToken = headerAccessor.getFirstNativeHeader("Authorization");

        if (authToken != null && authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7); // "Bearer " 제거

            // 2. JWT 토큰을 디코딩하여 사용자 ID 추출
            return jwtTokenProvider.getUserIdFromToken(authToken);
        }
        return null; // 인증되지 않은 사용자
    }
}
