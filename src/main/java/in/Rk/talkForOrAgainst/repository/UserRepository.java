package in.Rk.talkForOrAgainst.repository;

import in.Rk.talkForOrAgainst.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);

    Optional<Object> findByUserId(String ownerId);
}
