package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@Repository
public interface MessageRepository extends CrudRepository<Message, Integer> {

    @Override
    Optional<Message> findById(@Param("id") Integer id);

    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId ORDER BY m.createdAt")
    List<Message> findAllMessagesByMatchId(@Param("matchId") Integer matchId);
    
    @Query("SELECT m FROM Message m WHERE m.player.userPlayer.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findAllMessagesFromUser(@Param("userId") Integer userId);
    
    @Query("SELECT DISTINCT m.match FROM Message m WHERE m.player.userPlayer.id = :userId")
    List<Match> findAllChatsFromUser(@Param("userId") Integer userId);
    
    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId AND m.createdAt > :timestamp ORDER BY m.createdAt")
    List<Message> findNewMessages(@Param("matchId") Integer matchId, @Param("timestamp") LocalDateTime timestamp);
}
