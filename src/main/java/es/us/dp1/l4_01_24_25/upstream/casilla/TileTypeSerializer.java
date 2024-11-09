package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TileTypeSerializer extends JsonSerializer<TipoCasilla> {

    @Override
    public void serialize(TipoCasilla value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getTipo());
    }
    
}
