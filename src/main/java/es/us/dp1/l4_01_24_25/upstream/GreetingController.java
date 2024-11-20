package es.us.dp1.l4_01_24_25.upstream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import es.us.dp1.l4_01_24_25.upstream.player.JugadorService;

@Controller
public class GreetingController {

	@MessageMapping("/hello")
	@SendTo("/topic/refresh")
	public String greeting(Jugador message) throws Exception {
		return "Hello";
	}

}
