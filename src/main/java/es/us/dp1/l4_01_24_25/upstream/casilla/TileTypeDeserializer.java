package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class TileTypeDeserializer extends JsonDeserializer<TipoCasilla> {

    private TipoCasillaService tipoCasillaService;

    public TileTypeDeserializer(TipoCasillaService tipoCasillaService) {
        this.tipoCasillaService = tipoCasillaService;
    }

    @Override
    public TipoCasilla deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        TipoCasilla r = null;
        try {
            Integer i = p.getIntValue();
            r = this.tipoCasillaService.findById(i);
        } catch (Exception e) {
            throw new IOException();
        }
        return r;
    }
    
}
