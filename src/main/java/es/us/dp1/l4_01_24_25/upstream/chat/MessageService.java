package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import jakarta.transaction.Transactional;

@Service
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Transactional
    public Message createMessage(Player player, Match match, String content) {
        Message message = new Message();
        message.setPlayer(player);
        message.setMatch(match);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setDeleted(false);
        return messageRepository.save(message);
    }
    
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setDeleted(true);
        messageRepository.save(message);
    }
    
    public List<Message> getMatchMessages(Long matchId) {
        try {
            return messageRepository.findAllMessagesByMatchId(matchId);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching messages for match " + matchId, e);
        }
    }    
    
    public List<Message> getUserMessages(Long userId) {
        return messageRepository.findAllMessagesFromUser(userId);
    }
    
    public List<Match> getUserChats(Long userId) {
        return messageRepository.findAllChatsFromUser(userId);
    }
    
    public List<Message> getNewMessages(Long matchId, LocalDateTime since) {
        return messageRepository.findNewMessages(matchId, since);
    }
}
