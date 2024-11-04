package es.us.dp1.l4_01_24_25.upstream.partidaSalmon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/partidaSalmon")
@SecurityRequirement(name = "bearerAuth")
public class PartidaSalmonController {

    private final PartidaSalmonService partidaSalmonService;

    @Autowired
    public PartidaSalmonController(PartidaSalmonService partidaSalmonService){
        this.partidaSalmonService = partidaSalmonService;
    }

    @GetMapping(value="{id}")
    public ResponseEntity<PartidaSalmon> findById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(partidaSalmonService.findPartidaSalmon(id), HttpStatus.OK);
    }

}
