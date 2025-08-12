package in.Rk.talkForOrAgainst.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class PublicDebate {
    @Id
    @GeneratedValue
    private Long id;
    private String debateId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime createdAt;

    // getters/setters
}

