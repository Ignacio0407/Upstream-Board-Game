package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@Component
public class TileDeserializer extends JsonDeserializer<Tile>{

    private TileService tileService;
    
    public TileDeserializer(TileService tileService) {
        this.tileService = tileService;
    }

    @Override
    public Tile deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Tile r = null;
        try {
            Integer i = p.getIntValue();
            r = this.tileService.findById(i);
        } catch (Exception e) {
            throw new IOException();
        }
        return r;
    }
    
}
