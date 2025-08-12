package in.Rk.talkForOrAgainst.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String geminiApiKey;

    @Value("${openai.api.url}")
    private String geminiUrl; // e.g. https://api.openai.com/v1/chat/completions

    private final RestTemplate restTemplate;

    @PostConstruct
    public void validateKey() {
        System.out.println("🔐 Loaded OpenAI Key: " + geminiApiKey);
        System.out.println("🌐 OpenAI URL: " + geminiUrl);
    }

    public OpenAiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String analyzeDebate(Map<String, List<String>> userMessages, String debateTopic) {
        String prompt = buildPrompt(userMessages, debateTopic);
        return sendToGemini(prompt);
    }
    private String buildPrompt(Map<String, List<String>> userMessages, String debateTopic) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Analyze this debate on topic: \"" + debateTopic + "\".\n\n" +
                        "For each user, return the following in this exact format:\n\n" +
                        "User: [username]\n" +
                        "Feedback:\n" +
                        ". Strength \n" +
                        ". Weakness \n" +
                        ". Accuracy \n" +
                        ". Relevance \n" +
                        ". Improvement Tip \n\n" +
                        "Scores:\n" +
                        ". Accuracy: [1–10]\n" +
                        ". Relevance: [1–10]\n" +
                        ". Distinctiveness: [1–10]\n" +
                        ". Engagement: [1–10] (Follow-up & interaction)\n\n" +
                        "Rank this user among all participants based on overall contribution.\n\n" +
                        "Use this exact format for each user so it can be parsed and displayed cleanly.\n\n"
        );

        userMessages.forEach((username, messages) -> {
            sb.append("User ").append(username).append(":\n");
            messages.forEach(msg -> sb.append("- ").append(msg).append("\n"));
            sb.append("\n");
        });

        return sb.toString();
    }
     private String sendToGemini(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String fullUrl = geminiUrl + "?key=" + geminiApiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);
        return response.getBody();
    }

}
