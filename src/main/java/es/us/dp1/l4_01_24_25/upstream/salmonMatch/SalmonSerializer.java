package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;

@Component
public class SalmonSerializer extends JsonSerializer<Salmon>{

    @Override
    public void serialize(Salmon value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getId());
    }
    
}
