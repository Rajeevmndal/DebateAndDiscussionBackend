//package in.Rk.talkForOrAgainst.controller;
//
//import in.Rk.talkForOrAgainst.entity.Debate;
//import in.Rk.talkForOrAgainst.entity.Message;
//import in.Rk.talkForOrAgainst.io.MessageRequest;
//import in.Rk.talkForOrAgainst.repository.DebateRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Controller;
//
//import java.time.LocalDateTime;
//
//@Controller
//public class ChatController {
//
//    private final DebateRepository debateRepository;
//
//    @Autowired
//    public ChatController(DebateRepository debateRepository) {
//        this.debateRepository = debateRepository;
//    }
//
//    @MessageMapping("/sendMessage/{debateId}")        // Client sends message to /app/sendMessage/{debateId}
//    @SendTo("/topic/room/{debateId}")                 // Broadcast to all clients subscribed to /topic/room/{debateId}
//    public Message sendMessage(
//            @DestinationVariable String debateId,
//            MessageRequest request                    // ✅ No @RequestBody here!
//    ) {
//        System.out.println("🔔 Message received for debateId: " + debateId + " | From: " + request.getSender() + " | Content: " + request.getContent());
//
//        Debate debate = debateRepository.findByDebateId(debateId);
//
//        Message message = new Message();
//        message.setContent(request.getContent());
//        message.setSender(request.getSender());
//        message.setTimeStamp(LocalDateTime.now());
//
//        if (debate != null) {
//            debate.getMessages().add(message);
//            debateRepository.save(debate);
//        } else {
//            throw new RuntimeException("❌ Debate room not found for ID: " + debateId);
//        }
//
//        return message;  // ✅ Will be broadcasted to all subscribers of /topic/room/{debateId}
//    }
//}
