package upstream.chat;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.model.BaseServiceWithDTO;
import upstream.player.Player;
import upstream.player.PlayerService;

@Service
public class MessageService extends BaseServiceWithDTO<Message, MessageDTO, Integer>{

    private MessageRepository messageRepository;
    MessageMapper messageMapper;
    PlayerService playerService;
    MatchService matchService;
    SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository, MessageMapper messageMapper, PlayerService playerService, MatchService matchService, SimpMessagingTemplate messagingTemplate) {
        super(messageRepository, messageMapper);
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.playerService = playerService;
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }
    
    public List<MessageDTO> findMatchMessages(Integer matchId) {
        return this.messageRepository.findMessagesByMatchIdDTO(matchId);
    } 
    
    public List<MessageDTO> findUserMessages(Integer userId) {
        return this.messageRepository.findMessagesByUserIdDTO(userId);
    }
    
    public List<Match> findUserChats(Integer userId) {
        return this.messageRepository.findAllChatsFromUser(userId);
    }

    // XSS Protection: Clean the content before saving and broadcasting
    public static String sanitizeContent(String content) {
        return org.jsoup.Jsoup.clean(content, org.jsoup.safety.Safelist.none());
    }

    @Transactional
    public void saveAndSend(MessageDTO messageDTO) {
        Player p = this.playerService.findById(messageDTO.getPlayerId());
        Match m = this.matchService.findById(messageDTO.getMatchId());
        Message message = new Message(p, m, sanitizeContent(messageDTO.getContent()));
        this.messageRepository.save(message);
        MessageDTO dtoToSend = this.messageMapper.toDTO(message);
        this.messagingTemplate.convertAndSend("/topic/chat/" + m.getId(), dtoToSend);
    }

    @Override
    @Transactional
    protected Message updateEntityFields (Message newMessage, Message messageToUpdate) {
        messageToUpdate.setContent(newMessage.getContent());
        return messageToUpdate; 
    }

}