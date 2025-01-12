package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Message request) {
        Message message = messageService.createMessage(
            request.getPlayer(),
            request.getMatch(),
            request.getContent()
        );
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

    @PatchMapping
    public ResponseEntity<Message> saveMessage(@RequestBody Message messagePayload) {
        if (messagePayload.getContent() == null || messagePayload.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Validación de contenido vacío.
        }

        try {
            Message message = messageService.createMessage(
                messagePayload.getPlayer(),
                messagePayload.getMatch(),
                messagePayload.getContent().trim()
            );
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Manejo de errores inesperados.
        }
    }

}
