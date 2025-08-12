package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.DiscussionPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {
    List<DiscussionPost> findByDebate_DebateIdOrderByCreatedAtAsc(String debateId);
    Long countByAuthorUserName(String authorUserName);
}
