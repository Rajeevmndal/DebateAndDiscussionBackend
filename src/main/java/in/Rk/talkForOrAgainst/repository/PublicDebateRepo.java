package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.PublicDebate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicDebateRepo extends JpaRepository<PublicDebate, Long> {
    Optional<Object> findByDebateId(String debateId);
}
