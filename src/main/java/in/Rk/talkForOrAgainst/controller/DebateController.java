package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.Debate;
import in.Rk.talkForOrAgainst.entity.Message;
import in.Rk.talkForOrAgainst.entity.UserEntity;
import in.Rk.talkForOrAgainst.io.DebateRequest;
import in.Rk.talkForOrAgainst.repository.DebateMessageRepository;
import in.Rk.talkForOrAgainst.repository.DebateRepository;
import in.Rk.talkForOrAgainst.repository.UserRepository;
import in.Rk.talkForOrAgainst.service.EmailService;
import in.Rk.talkForOrAgainst.websocket.DebateWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/debates")
@RequiredArgsConstructor
public class DebateController {

    private final DebateRepository debateRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final DebateMessageRepository messageRepo;
    private final DebateWebSocketHandler debateWebSocketHandler;

    // ✅ Get the currently authenticated user using Spring Security
    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // from JWT
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // ✅ Create a new Debate
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createDebate(@RequestBody DebateRequest request, @CurrentSecurityContext(expression = "authentication?.name") String email) {
        UserEntity owner = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if(debateRepo.findByDebateId(request.getDebateId())!=null){
            return ResponseEntity.badRequest().body("Debate already exist");
        }

        Debate debate = Debate.builder()
                .topic(request.getTopic())
                .description(request.getDescription())
                .owner(owner)
                .participants(new HashSet<>())// initialize to avoid null
                .debateId(request.getDebateId())
                .build();

        // ✅ Automatically add the owner to participants
        debate.getParticipants().add(owner);
        debateRepo.save(debate);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("debateId", debate.getDebateId()));
    }

    // ✅ Invite users to participate in a debate
    @PostMapping("/{debateId}/invite")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> inviteUsers(@PathVariable String debateId, @RequestBody List<String> ids) {
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) {
            throw new RuntimeException("Debate not found");
        }

        UserEntity currentUser = getCurrentUser();

        if (!debate.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the debate owner can invite users.");
        }

        for (String userId : ids) {
            UserEntity user = userRepo.findByEmail(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            debate.getParticipants().add(user);
            emailService.sendAddedToDebateEmail(user.getEmail(), user.getName(), debate.getTopic(), debate.getDebateId());
        }
        debate.getParticipants().add(currentUser);
        debateRepo.save(debate);
        return ResponseEntity.ok("Users added and email sent.");
    }

    // ✅ Get all debates the logged-in user is participating in
    @GetMapping("/my-participated")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getMyDebates() {
        UserEntity user = getCurrentUser();
        List<Debate> debates = debateRepo.findAllByParticipants_Id(user.getId());
        return ResponseEntity.ok(debates);
    }

    @GetMapping("/{debateId}/message")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String debateId,
            @RequestParam(value="page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "20", required = false)int size
    ){
        Debate debate=debateRepo.findByDebateId(debateId);
        if(debate==null){
            return ResponseEntity.badRequest().build();
        }
        List<Message> messages=debate.getMessages();
        int start=Math.max(0,messages.size()-(page+1)*size);
        int end=Math.min(messages.size(),start+size);
        List<Message> paginatedMessage=messages.subList(start,end);
        return ResponseEntity.ok(paginatedMessage);
    }

    @Transactional
    @PostMapping("/{debateId}/start")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> startDebate(@PathVariable String debateId, @RequestBody Map<String, Integer> payload) {
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) return ResponseEntity.badRequest().body("Debate not found");

        UserEntity currentUser = getCurrentUser();
        if (!debate.getOwner().getId().equals(currentUser.getId())) {
            System.out.println("Debate Owner ID: " + debate.getOwner().getId());
            System.out.println("Current User ID: " + currentUser.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can start the debate");
        }

        // Delete all previous messages
        messageRepo.deleteByDebateId(debateId);

        // Lock the debate
        debate.setLocked(true);
        debateRepo.save(debate);

        // Broadcast timer via WebSocket
        int duration = payload.get("duration");
        debateWebSocketHandler.broadcastTimer(debateId, duration);

        return ResponseEntity.ok("Debate started with timer");
    }

    @PostMapping("/{debateId}/unlock")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> unlockDebate(@PathVariable String debateId) {
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) return ResponseEntity.badRequest().body("Debate not found");

        debate.setLocked(false);
        debateRepo.save(debate);

        return ResponseEntity.ok("Debate unlocked");
    }

    //get room: join
    @GetMapping("/{debateId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> joinDebate(
            @PathVariable String debateId
    ) {
        System.out.println("Hii i am join controller");
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) {
            return ResponseEntity.badRequest()
                    .body("Room not found!!");
        }
        UserEntity currentUser = getCurrentUser();
        // ✅ Check if current user is a participant
        if (!debate.getParticipants().contains(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not a participant in this debate.");
        }

        if (Boolean.TRUE.equals(debate.getLocked()) && !debate.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Debate is in progress. Please wait until it ends.");
        }
        if (Boolean.TRUE.equals(debate.getLocked()) && debate.getOwner().getId().equals(currentUser.getId())) {
            debate.setLocked(false);
            debateRepo.save(debate);
        }
        return ResponseEntity.ok(debate);
    }

    @GetMapping("/debateCount")
    public ResponseEntity<Long> getNumberOfDebate(){
        UserEntity user=getCurrentUser();
        Long count=debateRepo.countByOwner_Id(user.getId());
        System.out.println(count);
        return new ResponseEntity<>(count,HttpStatus.OK);
    }

    @GetMapping("/{debateId}/metadata")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getDebateMetadata(@PathVariable String debateId) {
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) {
            return ResponseEntity.badRequest().body("Debate not found");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("debateId", debate.getDebateId());
        metadata.put("topic", debate.getTopic());
        metadata.put("ownerUsername", debate.getOwner().getName());
        metadata.put("locked", debate.getLocked());

        return ResponseEntity.ok(metadata);
    }

    @Transactional
    @DeleteMapping("/{debateId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<?> deleteDebate(@PathVariable String debateId) {
        Debate debate = debateRepo.findByDebateId(debateId);
        if (debate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Debate not found");
        }

        UserEntity currentUser = getCurrentUser();
        if (!debate.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can delete this debate");
        }

        // Delete associated messages first
        messageRepo.deleteByDebateId(debateId);

        // Delete the debate itself
        debateRepo.delete(debate);

        return ResponseEntity.ok("Debate deleted successfully");
    }


}
