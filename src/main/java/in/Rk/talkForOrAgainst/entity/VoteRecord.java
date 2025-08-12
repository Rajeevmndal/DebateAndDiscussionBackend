package in.Rk.talkForOrAgainst.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteRecord {
    @Id
    @GeneratedValue
    private Long id;

    private String username; // Extracted from JWT

    @ManyToOne
    @JsonIgnore
    private DiscussionPost post;

    private String direction; // "up" or "down"

    private LocalDateTime timestamp;
}
