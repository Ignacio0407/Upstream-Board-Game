package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.partida.Partida;
import es.us.dp1.l4_01_24_25.upstream.partida.PartidaService;

public class MatchDeserializer extends JsonDeserializer<Partida> {

    private PartidaService partidaService;

    public MatchDeserializer(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @Override
    public Partida deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Partida r = null;

        try {
            Integer pId = p.getIntValue();
            r = this.partidaService.getPartidaById(pId);
        } catch (Exception e) {
            throw new IOException("Partida no encontrada con esa id: " + p.getValueAsString());
        }
        return r;
    }
    
}
