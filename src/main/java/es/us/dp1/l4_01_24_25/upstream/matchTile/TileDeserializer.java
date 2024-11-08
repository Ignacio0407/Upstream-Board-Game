package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.casilla.Casilla;
import es.us.dp1.l4_01_24_25.upstream.casilla.CasillaService;

public class TileDeserializer extends JsonDeserializer<Casilla>{

    private CasillaService casillaService;
    
    public TileDeserializer(CasillaService casillaService) {
        this.casillaService = casillaService;
    }

    @Override
    public Casilla deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Casilla r = null;
        try {
            Integer i = p.getIntValue();
            r = this.casillaService.findById(i).get();
        } catch (Exception e) {
            throw new IOException();
        }
        return r;
    }
    
}
