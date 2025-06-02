package es.us.dp1.l4_01_24_25.upstream.match;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@Component
public class PlayerDeserializer extends JsonDeserializer<Player> {

    private final PlayerService jugadorService;

    public PlayerDeserializer(PlayerService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @Override
    public Player deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Player r = null;
        try {
            Integer jid = p.getIntValue();
            r = this.jugadorService.findById(jid);
        } catch (Exception e) {
            throw new IOException("Id not found: " + p.getIntValue());
        }
        return r;
    }
    
}
