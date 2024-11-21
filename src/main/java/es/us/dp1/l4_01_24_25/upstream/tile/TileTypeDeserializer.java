package es.us.dp1.l4_01_24_25.upstream.tile;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class TileTypeDeserializer extends JsonDeserializer<TileType> {

    private TileTypeService tileTypeService;

    public TileTypeDeserializer(TileTypeService tipoCasillaService) {
        this.tileTypeService = tipoCasillaService;
    }

    @Override
    public TileType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        TileType r = null;
        try {
            Integer i = p.getIntValue();
            r = this.tileTypeService.findById(i);
        } catch (Exception e) {
            throw new IOException();
        }
        return r;
    }
    
}
