package es.us.dp1.l4_01_24_25.upstream.match;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

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
            r = this.jugadorService.getJugadorById(jid);
        } catch (Exception e) {
            throw new IOException("Id not found: " + p.getIntValue());
        }
        return r;
    }
    
}
