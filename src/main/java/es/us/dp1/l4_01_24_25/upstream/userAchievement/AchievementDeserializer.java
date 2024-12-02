package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;

@Component
public class AchievementDeserializer extends JsonDeserializer<Achievement>{

    @Autowired
    private AchievementService achievementService;

    @Override
    public Achievement deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        Achievement a = null;
        
        try {
            Integer aId = p.getIntValue();
            a = this.achievementService.getById(aId);
        } catch (Exception e) {
            throw new IOException("Achievement not found: " + p.getIntValue());
        }
        return a;
    }
    
}
