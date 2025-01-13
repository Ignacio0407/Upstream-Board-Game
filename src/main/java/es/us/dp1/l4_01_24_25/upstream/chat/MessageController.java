package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }
    
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageRequest request) {
        Message message = messageService.createMessage(request.getPlayerId(), request.getMatchId(), request.getContent());
        messagingTemplate.convertAndSend("/topic/chat/" + request.getMatchId(), message);
        return ResponseEntity.ok(message);
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<Message>> getMatchMessages(@PathVariable Long matchId) {
        List<Message> messages = messageService.getMatchMessages(matchId);
        return ResponseEntity.ok(messages != null ? messages : new ArrayList<>());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Message>> getUserMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getUserMessages(userId));
    }
    
    @GetMapping("/chats/{userId}")
    public ResponseEntity<List<Match>> getUserChats(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getUserChats(userId));
    }

}
