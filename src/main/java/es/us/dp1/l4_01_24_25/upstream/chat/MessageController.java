package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController extends BaseRestControllerWithDTO<Message, MessageDTO, Integer> {
    
    MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        super(messageService);
        this.messageService = messageService;
    }

    @PostMapping
    public void saveAndSend(@Valid MessageDTO messageDto) {
        this.messageService.saveAsDTO(messageDto);
    }
    
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MessageDTO>> findMatchMessages(@PathVariable Integer matchId) {
        return ResponseEntity.ok(messageService.findMatchMessages(matchId));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageDTO>> findUserMessages(@PathVariable Integer userId) {
        return ResponseEntity.ok(messageService.findUserMessages(userId));
    }
    
    @GetMapping("/chats/{userId}")
    public ResponseEntity<List<Match>> findUserChats(@PathVariable Integer userId) {
        return ResponseEntity.ok(messageService.findUserChats(userId));
    }

}