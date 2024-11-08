package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.casilla.Casilla;

public class TileSerializer extends JsonSerializer<Casilla> {

    @Override
    public void serialize(Casilla value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getId());
    }
    
}
