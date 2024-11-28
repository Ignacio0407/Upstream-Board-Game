package es.us.dp1.l4_01_24_25.upstream.player;
//ESTO ES NECESARIO

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@Component
public class MatchSerializer extends JsonSerializer<Match>{

    @Override
    public void serialize(Match value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
            gen.writeNumber(value.getId());
        
        }
}