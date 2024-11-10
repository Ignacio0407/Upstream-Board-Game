package es.us.dp1.l4_01_24_25.upstream.partida;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.player.Jugador;

public class PlayerSerializer extends JsonSerializer<Jugador> {

    @Override
    public void serialize(Jugador value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getId());
    }
    
}
