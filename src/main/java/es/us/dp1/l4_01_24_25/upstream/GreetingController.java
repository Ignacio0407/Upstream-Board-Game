package es.us.dp1.l4_01_24_25.upstream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

@Controller
public class GreetingController {

	@MessageMapping("/hello")
	@SendTo("/topic/refresh")
	public String greeting(Player message) throws Exception {
		return "Hello";
	}

	@MessageMapping("/start")
	@SendTo("/topic/game")
	public String greeting2(Player message) throws Exception{
		return "Hello";
	}

	@MessageMapping("/dash")
	@SendTo("/topic/reload")
	public String greeting3(Player message) throws Exception{
		return "Hello";
	}

	@MessageMapping("/fetch")
	@SendTo("/topic/`players")
	public String greeting4(Player message) throws Exception{
		return "Hello";
	}
}
