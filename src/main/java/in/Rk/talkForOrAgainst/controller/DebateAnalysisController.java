package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.Debate;
import in.Rk.talkForOrAgainst.entity.DebateMessage;
import in.Rk.talkForOrAgainst.repository.DebateMessageRepository;
import in.Rk.talkForOrAgainst.repository.DebateRepository;
import in.Rk.talkForOrAgainst.service.OpenAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import in.Rk.talkForOrAgainst.websocket.DebateWebSocketHandler;

@RestController
@RequestMapping("/api/debate")
public class DebateAnalysisController {

    private final OpenAiService openAiService;
    private final DebateWebSocketHandler debateWebSocketHandler;
    private final DebateMessageRepository debateMessageRepository;
    private final DebateRepository debateRepo;

    public DebateAnalysisController(OpenAiService openAiService, DebateWebSocketHandler debateWebSocketHandler,DebateMessageRepository debateMessageRepository, DebateRepository debateRepo) {
        this.openAiService = openAiService;
        this.debateWebSocketHandler = debateWebSocketHandler;
        this.debateMessageRepository=debateMessageRepository;
        this.debateRepo=debateRepo;
    }
    private Map<String, List<String>> groupMessagesByUser(List<DebateMessage> messages) {
        return messages.stream()
                .collect(Collectors.groupingBy(
                        DebateMessage::getSenderUsername, // or getUsername if applicable
                        Collectors.mapping(DebateMessage::getMessage, Collectors.toList())
                ));
    }

    @PostMapping("/{debateId}/analyze")
    public ResponseEntity<String> analyzeDebate(@PathVariable String debateId) {
        try {
            System.out.println("🔍 Received analysis request for room: " + debateId);

            // 🗄️ Fetch messages from DB
            Debate debate=debateRepo.findByDebateId(debateId);
            String debateTopic=debate.getTopic();
            List<DebateMessage> messages = debateMessageRepository.findByDebateIdOrderByTimestampAsc(debateId);
            System.out.println("📥 Fetched " + messages.size() + " messages from DB");

            // 🧠 Group messages by user
            Map<String, List<String>> userMessages = groupMessagesByUser(messages);
            System.out.println("🧠 Grouped messages by user: " + userMessages.keySet());

            // 🤖 Analyze via OpenAI
            String analysis = openAiService.analyzeDebate(userMessages,debateTopic);
            System.out.println("✅ OpenAI analysis completed");

            // 📢 Broadcast to room
            debateWebSocketHandler.broadcastAnalysis(debateId, analysis);
            System.out.println("📢 Analysis broadcasted to room: " + debateId);

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            System.err.println("❌ Error during analysis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Analysis failed: " + e.getMessage());
        }
    }

}