
package upstream.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
        "/dashboard",
        "/login",
        "/register",
        "/plans",
        "/rules",
        "/users",
        "/developers",
        "/creategame",
        "/docs",
        "/do-not-hack-my-app",
        "/achievements",
        "/achievements/",
        "/achievements/new",
        "/matches/{matchId}/waiting",
        "/matches/{matchId}/game",
        "/matches/{matchId}/end"
    })
    public String forwardToFrontend() {
        return "forward:/index.html";
    }
}