package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@Service
public class MessageService extends BaseServiceWithDTO<Message, MessageDTO, Integer>{

    private MessageRepository messageRepository;
    MessageMapper messageMapper;
    PlayerService playerService;
    MatchService matchService;

    public MessageService(MessageRepository messageRepository, MessageMapper messageMapper, PlayerService playerService, MatchService matchService) {
        super(messageRepository, messageMapper);
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.playerService = playerService;
        this.matchService = matchService;
    }
    
    public List<MessageDTO> findMatchMessagesAsDTO(Integer matchId) {
        return this.findList(messageRepository.findMessagesByMatchIdDTO(matchId));
    } 
    
    public List<MessageDTO> findUserMessages(Integer userId) {
        return this.findList(messageRepository.findMessagesByUserIdDTO(userId));
    }
    
    public List<Match> findUserChats(Integer userId) {
        return this.findList(messageRepository.findAllChatsFromUser(userId));
    }

    @Override
    @Transactional
    public MessageDTO saveAsDTO(MessageDTO messageDTO) {
        Player p = playerService.findById(messageDTO.getPlayerId());
        Match m = matchService.findById(messageDTO.getMatchId());
        Message message = new Message(p, m, messageDTO.getContent());
        messageRepository.save(message);
        return messageMapper.toDTO(message);
    }

    @Override
    @Transactional
    protected void updateEntityFields (Message newMessage, Message messageToUpdate) {
        messageToUpdate.setPlayer(newMessage.getPlayer());
        messageToUpdate.setContent(newMessage.getContent());
        messageToUpdate.setCreatedAt(newMessage.getCreatedAt());
        messageToUpdate.setMatch(newMessage.getMatch());
    }

}