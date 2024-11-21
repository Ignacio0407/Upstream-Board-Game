package es.us.dp1.l4_01_24_25.upstream.tile;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TileTypeSerializer extends JsonSerializer<TileType> {

    @Override
    public void serialize(TileType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getType());
    }
    
}
