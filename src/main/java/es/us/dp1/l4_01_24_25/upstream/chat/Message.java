package es.us.dp1.l4_01_24_25.upstream.chat;

import java.time.LocalDateTime;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Match match;

    public Message(Player player, Match match, String content) {
        this.player = player;
        this.match = match;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Message() {
        this.createdAt = LocalDateTime.now();
    }

}