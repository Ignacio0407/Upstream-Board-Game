package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

public class UserDeserializer extends JsonDeserializer<User>{

    private UserService userService;

    public UserDeserializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        User r = null;

        try {
            Integer uId = p.getIntValue();
            r = this.userService.findUser(uId);
        } catch (Exception e) {
            throw new IOException("Id not found: " + p.getIntValue());
        }
        return r;
    }

}
