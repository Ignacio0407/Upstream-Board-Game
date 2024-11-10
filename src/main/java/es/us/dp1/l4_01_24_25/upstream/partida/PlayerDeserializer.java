package es.us.dp1.l4_01_24_25.upstream.partida;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import es.us.dp1.l4_01_24_25.upstream.player.JugadorService;

public class PlayerDeserializer extends JsonDeserializer<Jugador> {

    private final JugadorService jugadorService;

    public PlayerDeserializer(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @Override
    public Jugador deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Jugador r = null;
        try {
            Integer jid = p.getIntValue();
            r = this.jugadorService.getJugadorById(jid);
        } catch (Exception e) {
            throw new IOException("Id not found: " + p.getIntValue());
        }
        return r;
    }
    
}
