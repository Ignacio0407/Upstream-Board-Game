package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final MatchService matchService;
    private final PlayerService playerService;

    @Autowired
    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate, UserService userService, MatchService matchService, PlayerService playerService) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.matchService = matchService;
        this.playerService = playerService;
    }

    @PostMapping("/{matchId}/{playerId}/{message}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Message> createMessage(@PathVariable Integer matchId, @PathVariable Integer playerId, @PathVariable String message) {
            System.out.println("Request received: matchId=" + matchId + ", playerId=" + playerId + ", message=" + message);

            if (playerService.getById(playerId) == null) throw new ResourceNotFoundException("Error encontrando al jugador");
            if (matchService.getById(matchId) == null) throw new ResourceNotFoundException("Error encontrando al jugador");

            Message createdMessage = messageService.createMessage(playerId, matchId, message);
            System.out.println("Message created: " + createdMessage);

            // Enviar mensaje al WebSocket
            messagingTemplate.convertAndSend("/topic/chat/" + matchId, createdMessage);
            return ResponseEntity.ok(createdMessage);
        }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer messageId) {
        if (messageService.findById(messageId) == null) throw new ResourceNotFoundException("Ha habido un problema encontrando el mensaje a borrar");
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<Message>> getMatchMessages(@PathVariable Integer matchId) {
        if (matchService.getById(matchId) == null) throw new ResourceNotFoundException("Ha habido un error encontrando la partida");
        List<Message> messages = messageService.getMatchMessages(matchId);
        return ResponseEntity.ok(messages != null ? messages : new ArrayList<>());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Message>> getUserMessages(@PathVariable Integer userId) {
        if (userService.findUser(userId) == null) throw new ResourceNotFoundException("Ha habido un error encontrando al usuario");
        return ResponseEntity.ok(messageService.getUserMessages(userId));
    }
    
    @GetMapping("/chats/{userId}")
    public ResponseEntity<List<Match>> getUserChats(@PathVariable Integer userId) {
        if (userService.findUser(userId) == null) throw new ResourceNotFoundException("Ha habido un error encontrando al usuario");
        return ResponseEntity.ok(messageService.getUserChats(userId));
    }

}
