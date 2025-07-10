package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.chat.MessageDTO(m.id, m.player.id, m.player.name, m.match.id, m.content) FROM Message m WHERE m.match.id = :matchId ORDER BY m.createdAt")
    List<MessageDTO> findMessagesByMatchIdDTO(@Param("matchId") Integer matchId);

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.chat.MessageDTO(m.id, m.player.id, m.player.name, m.match.id, m.content) FROM Message m WHERE m.player.userPlayer.id = :userId ORDER BY m.createdAt DESC")
    List<MessageDTO> findMessagesByUserIdDTO(@Param("userId") Integer userId);
    
    @Query("SELECT DISTINCT m.match FROM Message m WHERE m.player.userPlayer.id = :userId")
    List<Match> findAllChatsFromUser(@Param("userId") Integer userId);
}
