package in.Rk.talkForOrAgainst.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private Long debateId;
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}
