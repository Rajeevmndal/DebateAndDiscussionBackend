package in.Rk.talkForOrAgainst.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.Rk.talkForOrAgainst.entity.DebateMessage;
import in.Rk.talkForOrAgainst.repository.DebateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DebateWebSocketHandler extends TextWebSocketHandler {

    private final DebateMessageRepository messageRepository;

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Map<WebSocketSession, String> sessionUsernames = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("✅ WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> msg = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) msg.get("type");

        if ("join".equals(type)) {
            String username = (String) msg.get("username");
            sessionUsernames.put(session, username);
            broadcastActiveUsers();
            System.out.println("👤 User joined: " + username);

        } else if ("chat".equals(type)) {
            // Parse required message fields
            String debateId = (String) msg.get("debateId");
            String sender = (String) msg.get("sender");
            String role = (String) msg.get("role");
            String content = (String) msg.get("message");

            // Save message to DB
            DebateMessage dbMessage = DebateMessage.builder()
                    .debateId(debateId)
                    .senderUsername(sender)
                    .role(role)
                    .message(content)
                    .timestamp(LocalDateTime.now())
                    .build();

            messageRepository.save(dbMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "chat");
            response.put("debateId", debateId);
            response.put("sender", sender);
            response.put("side", role); // align with frontend filter key
            response.put("content", content); // match frontend display key
            response.put("timestamp", LocalDateTime.now().toString());

            String jsonResponse = objectMapper.writeValueAsString(response);

            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(jsonResponse));
                }
            }

        }
    }

    public void broadcastTimer(String debateId, int durationMinutes) {
        Map<String, Object> timerPayload = new HashMap<>();
        timerPayload.put("type", "timer");
        timerPayload.put("debateId", debateId);
        timerPayload.put("duration", durationMinutes);
        timerPayload.put("startTime", LocalDateTime.now().toString());

        try {
            String json = objectMapper.writeValueAsString(timerPayload);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastAnalysis(String debateId, String analysisText) {
        Map<String, Object> analysisPayload = new HashMap<>();
        analysisPayload.put("type", "analysis");
        analysisPayload.put("debateId", debateId);
        analysisPayload.put("content", analysisText);
        analysisPayload.put("timestamp", LocalDateTime.now().toString());

        try {
            String json = objectMapper.writeValueAsString(analysisPayload);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // or use your logger
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionUsernames.remove(session);
        System.out.println("❌ WebSocket disconnected: " + session.getId());
        try {
            broadcastActiveUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastActiveUsers() throws IOException {
        List<String> users = new ArrayList<>(sessionUsernames.values());
        Map<String, Object> update = new HashMap<>();
        update.put("type", "activeUsers");
        update.put("users", users);

        String json = objectMapper.writeValueAsString(update);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }
}
