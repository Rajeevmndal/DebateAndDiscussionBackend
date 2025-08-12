package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.DebateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DebateMessageRepository extends JpaRepository<DebateMessage, Long> {

    // Get all messages from a specific debate
    List<DebateMessage> findByDebateIdOrderByTimestampAsc(String debateId);

    // Get all messages sent by a specific user
    List<DebateMessage> findBySenderUsernameOrderByTimestampAsc(String username);

    // Get messages sent by a user in a specific debate
    List<DebateMessage> findByDebateIdAndSenderUsernameOrderByTimestampAsc(String debateId, String username);

    // For cleanup
    void deleteByTimestampBefore(LocalDateTime cutoff);

    void deleteByDebateId(String debateId);


}

