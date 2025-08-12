package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.DiscussionPost;
import in.Rk.talkForOrAgainst.entity.PublicDebate;
import in.Rk.talkForOrAgainst.entity.UserEntity;
import in.Rk.talkForOrAgainst.entity.VoteRecord;
import in.Rk.talkForOrAgainst.repository.DiscussionPostRepository;
import in.Rk.talkForOrAgainst.repository.PublicDebateRepo;
import in.Rk.talkForOrAgainst.repository.UserRepository;
import in.Rk.talkForOrAgainst.repository.VoteRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class DiscussionPostController {

    private final DiscussionPostRepository postRepo;
    private final PublicDebateRepo debateRepo;
    private final VoteRecordRepository voteRecordRepo;
    private final UserRepository userRepo;

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // from JWT
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @GetMapping("/{debateId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public List<DiscussionPost> getPosts(@PathVariable String debateId) {
        return postRepo.findByDebate_DebateIdOrderByCreatedAtAsc(debateId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public DiscussionPost createPost(@RequestBody PostRequest req) {
        PublicDebate debate = (PublicDebate) debateRepo.findByDebateId(req.debateId())
                .orElseThrow(() -> new RuntimeException("Debate not found"));

        DiscussionPost post = DiscussionPost.builder()
                .author(req.author())
                .authorUserName(req.authorUserName())
                .content(req.content())
                .side(DiscussionPost.Side.valueOf(req.side().toUpperCase()))
                .votes(0)
                .createdAt(LocalDateTime.now())
                .debate(debate)
                .build();

        return postRepo.save(post);
    }


    @PostMapping("/{postId}/vote")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<String> votePost(@PathVariable Long postId,
                                           @RequestParam String direction,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        // 🔍 Check if vote already exists for this user and post
        Optional<VoteRecord> existingVote = voteRecordRepo.findByUsernameAndPost_Id(username, postId);
        if (existingVote.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You have already voted on this post");
        }

        // ✅ Allow vote and store VoteRecord
        DiscussionPost post = postRepo.findById(postId).orElseThrow();
        if ("up".equalsIgnoreCase(direction)) post.setVotes(post.getVotes() + 1);
        else if ("down".equalsIgnoreCase(direction)) post.setVotes(post.getVotes() - 1);

        postRepo.save(post);

        voteRecordRepo.save(VoteRecord.builder()
                .username(username)
                .direction(direction)
                .post(post)
                .timestamp(LocalDateTime.now())
                .build());

        return ResponseEntity.ok("Vote recorded");
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Remove MODERATOR role if needed
    public ResponseEntity<String> deletePost(@PathVariable Long postId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Optional<DiscussionPost> optionalPost = postRepo.findById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        DiscussionPost post = optionalPost.get();
        String requester = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!requester.equals(post.getAuthorUserName()) && !isAdmin) {
            System.out.println(post.getAuthorUserName()+"  "+requester);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this post");
        }

        postRepo.deleteById(postId);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @GetMapping("/commentCount")
    public ResponseEntity<Long> getCommentCount(){
        UserEntity user=getCurrentUser();
        Long count=postRepo.countByAuthorUserName(user.getEmail());
        return new ResponseEntity<>(count,HttpStatus.OK);
    }


    public record PostRequest(String content, String side, String author, String debateId, String authorUserName) {}
}
