package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/partidaCasilla")
@SecurityRequirement(name = "bearerAuth")
class PartidaCasillaController {

    private final PartidaCasillaService partidaCasillaService;

    @Autowired
    public PartidaCasillaController(PartidaCasillaService partidaCasillaService){
        this.partidaCasillaService = partidaCasillaService;
    }

    



    
}
