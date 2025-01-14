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

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerService playerService;
    private final UserService userService;
    private final MatchService matchService;

    @Autowired
    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate, PlayerService playerService, UserService userService, MatchService matchService) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.playerService = playerService;
        this.userService = userService;
        this.matchService = matchService;
    }
    
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageRequest request) {
        try {
            if (playerService.getById(request.getPlayerId()) == null) throw new ResourceNotFoundException("Ha habido un error encontrando al jugador");
            if (matchService.getById(request.getMatchId()) == null) throw new ResourceNotFoundException("Ha habido un error encontrando la partida");
            
            Message message = messageService.createMessage(request.getPlayerId(), request.getMatchId(), request.getContent());
            messagingTemplate.convertAndSend("/topic/chat/" + request.getMatchId(), message);
            return ResponseEntity.ok(message);
        } catch (ResourceNotFoundException ex) {
            // Este bloque ya maneja los 404
            throw ex;
        } catch (Exception ex) {
            // Captura cualquier otra excepción inesperada y retorna un 500
            return ResponseEntity.status(500).body(null); // o puedes agregar un mensaje de error más informativo
        }
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
