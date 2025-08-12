package in.Rk.talkForOrAgainst.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String debateId;         // ID of the debate

    private String senderUsername;   // User who sent the message

    private String role;             // e.g., "admin", "user"

    @Column(columnDefinition = "TEXT")
    private String message;          // Actual message content

    private LocalDateTime timestamp; // When the message was sent

    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now();
    }
}

