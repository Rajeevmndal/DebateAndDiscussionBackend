package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.Debate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DebateRepository extends JpaRepository<Debate, Long> {

    List<Debate> findAllByParticipants_Id(Long userId);
    Debate findByDebateId(String debateId);
    Long countByOwner_Id(Long ownerId);
}