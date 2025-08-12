package in.Rk.talkForOrAgainst.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Debate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private String description;

    private String debateId;

    @OneToMany(mappedBy = "debate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages= new ArrayList<>();

    @ManyToOne
    private UserEntity owner;

    @ManyToMany
    private Set<UserEntity> participants = new HashSet<>();

    private boolean locked = false;


    public Boolean getLocked() {
        return locked;
    }
}
