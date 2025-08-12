package in.Rk.talkForOrAgainst.io;

import lombok.Data;

@Data
public class DebateRequest {
    private String topic;
    private String description;
    private Long ownerId;
    private String debateId;
}