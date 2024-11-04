package es.us.dp1.l4_01_24_25.upstream.player;


import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.partida.Partida;

@Component
public class matchSerializer extends JsonSerializer<Partida>{

    @Override
    public void serialize(Partida value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
            gen.writeNumber(value.getId());
        }
}