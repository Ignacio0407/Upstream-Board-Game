package es.us.dp1.l4_01_24_25.upstream.user;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class AuthoritySerializer extends JsonSerializer<Authorities> {

    @Override
    public void serialize(Authorities value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getAuthority());
    }
    
}
