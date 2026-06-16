package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;

@Component
public class MatchDeserializer extends JsonDeserializer<Match> {

    private MatchService matchService;

    public MatchDeserializer(MatchService partidaService) {
        this.matchService = partidaService;
    }

    @Override
    public Match deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Match r = null;

        try {
            Integer pId = p.getIntValue();
            r = this.matchService.getById(pId);
        } catch (Exception e) {
            throw new IOException("Partida no encontrada con esa id: " + p.getValueAsString());
        }
        return r;
    }
    
}
