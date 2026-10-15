package upstream.chat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import upstream.match.Match;
import upstream.model.BaseEntity;
import upstream.player.Player;
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
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Message() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

}