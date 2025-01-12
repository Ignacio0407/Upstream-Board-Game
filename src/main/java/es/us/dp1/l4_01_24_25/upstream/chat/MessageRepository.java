package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import es.us.dp1.l4_01_24_25.upstream.match.Match;


public interface MessageRepository extends CrudRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId AND m.deleted = false ORDER BY m.createdAt")
    List<Message> findAllMessagesByMatchId(@Param("matchId") Long matchId);
    
    @Query("SELECT m FROM Message m WHERE m.player.userPlayer.id = :userId AND m.deleted = false ORDER BY m.createdAt DESC")
    List<Message> findAllMessagesFromUser(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT m.match FROM Message m WHERE m.player.userPlayer.id = :userId")
    List<Match> findAllChatsFromUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId AND m.createdAt > :timestamp AND m.deleted = false ORDER BY m.createdAt")
    List<Message> findNewMessages(@Param("matchId") Long matchId, @Param("timestamp") LocalDateTime timestamp);
}
