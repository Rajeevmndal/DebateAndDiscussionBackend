package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.DebateMessage;
import in.Rk.talkForOrAgainst.repository.DebateMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class DebateMessageController {

    private final DebateMessageRepository messageRepository;

    @GetMapping("/by-debate/{debateId}")
    public List<DebateMessage> getMessagesByDebate(@PathVariable String debateId) {
        return messageRepository.findByDebateIdOrderByTimestampAsc(debateId);
    }

    @GetMapping("/by-user/{username}")
    public List<DebateMessage> getMessagesByUser(@PathVariable String username) {
        return messageRepository.findBySenderUsernameOrderByTimestampAsc(username);
    }

    @GetMapping("/by-debate-user/{debateId}/{username}")
    public List<DebateMessage> getMessagesByDebateAndUser(@PathVariable String debateId, @PathVariable String username) {
        return messageRepository.findByDebateIdAndSenderUsernameOrderByTimestampAsc(debateId, username);
    }
}

