package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;

@Component
public class AchievementSerializer extends JsonSerializer<Achievement>{

    @Override
    public void serialize(Achievement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getId());    
    }
    
}
