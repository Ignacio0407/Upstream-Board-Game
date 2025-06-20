package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRestControllerWithDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController extends BaseRestControllerWithDTO<Message, MessageDTO, Integer> {
    
    MessageService messageService;
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        super(messageService);
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageDTO> save(@Valid MessageDTO messageDto) {

            MessageDTO createdMessage = messageService.saveAsDTO(messageDto);
            // Send message to WebSocket
            messagingTemplate.convertAndSend("/topic/chat/" + createdMessage.getMatchId(), createdMessage);
            return ResponseEntity.ok(createdMessage);
        }
    
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MessageDTO>> findMatchMessages(@PathVariable Integer matchId) {
        List<MessageDTO> messages = messageService.findMatchMessagesAsDTO(matchId);
        return ResponseEntity.ok(messages != null ? messages : new ArrayList<>());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageDTO>> findUserMessages(@PathVariable Integer userId) {
        return ResponseEntity.ok(messageService.findUserMessages(userId));
    }
    
    @GetMapping("/chats/{userId}")
    public ResponseEntity<List<Match>> getUserChats(@PathVariable Integer userId) {
        return ResponseEntity.ok(messageService.findUserChats(userId));
    }

}