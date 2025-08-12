package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {
    Optional<VoteRecord> findByUsernameAndPost_Id(String username, Long postId);
}
