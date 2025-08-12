package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.PublicDebate;
import in.Rk.talkForOrAgainst.repository.PublicDebateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/public-debates")
public class PublicDebateController {

    @Autowired
    private PublicDebateRepo debateRepo;

    // Create new public debate
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public PublicDebate createDebate(@RequestBody PublicDebate debate) {
        debate.setCreatedAt(LocalDateTime.now());
        return debateRepo.save(debate);
    }

    // Get all public debates
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public List<PublicDebate> getAllDebates() {
        return debateRepo.findAll();
    }

    // Get debate by debateId
    @GetMapping("/{debateId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public PublicDebate getDebateByDebateId(@PathVariable String debateId) {
        return (PublicDebate) debateRepo.findByDebateId(debateId)
                .orElseThrow(() -> new RuntimeException("Debate not found with ID: " + debateId));
    }
}
