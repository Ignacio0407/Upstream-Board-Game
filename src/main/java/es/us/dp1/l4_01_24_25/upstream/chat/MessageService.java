package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchRepository;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerRepository;
import jakarta.transaction.Transactional;

@Service
public class MessageService extends BaseService<Message,Integer>{
    
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MatchRepository matchRepository;

    public MessageService(MessageRepository messageRepository, PlayerRepository playerRepository, MatchRepository matchRepository) {
        super(messageRepository);
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
    }
    
    /*@Transactional
    public Message createMessage2(Player player, Match match, String content) {
        Message message = new Message();
        message.setPlayer(player);
        message.setMatch(match);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setDeleted(false);
        return messageRepository.save(message);
    } */

    public Message findById(Integer id) {
        return messageRepository.findById(id).orElseGet(null);
    }

    public List<Message> getMatchMessages(Integer matchId) {
        try {
            List<Message> messages = messageRepository.findAllMessagesByMatchId(matchId);
            return messages == null ? new ArrayList<>() : messages;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching messages for match " + matchId, e);
        }
    }    
    
    public List<Message> getUserMessages(Integer userId) {
        return messageRepository.findAllMessagesFromUser(userId);
    }
    
    public List<Match> getUserChats(Integer userId) {
        return messageRepository.findAllChatsFromUser(userId);
    }
    
    public List<Message> getNewMessages(Integer matchId, LocalDateTime since) {
        return messageRepository.findNewMessages(matchId, since);
    }

    @Transactional
    public Message create(Integer playerId, Integer matchId, String content) {

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new ResourceNotFoundException("Player not found"));
            
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
            
        Message message = new Message(player, match, content);
        return messageRepository.save(message);
    }

}