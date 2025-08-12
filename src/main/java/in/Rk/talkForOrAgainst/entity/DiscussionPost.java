package in.Rk.talkForOrAgainst.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiscussionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String authorUserName;
    private String content;
    private int votes;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Side side;

    @ManyToOne
    @JoinColumn(name = "debate_id")
    private PublicDebate debate;


    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<VoteRecord> voteRecords;

    public enum Side {
        FOR, AGAINST
    }
}
