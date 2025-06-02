package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;

@Component
public class SalmonDeserializer extends JsonDeserializer<Salmon>{

    @Autowired
    private SalmonService salmonService;

    @Override
    public Salmon deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Salmon s = null;
        try {
            Integer sId = p.getIntValue();
            s = this.salmonService.findById(sId);
        } catch (Exception e) {
            throw new IOException("Salmon not found: " + p.getIntValue());
        } 
        return s;
    } 
    
}
