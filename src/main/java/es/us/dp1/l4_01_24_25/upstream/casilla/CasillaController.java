package es.us.dp1.l4_01_24_25.upstream.casilla;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/casilla")
@Tag(name = "Casilla", description = "Casilla API")
public class CasillaController {
    
}
