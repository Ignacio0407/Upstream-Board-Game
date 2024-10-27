package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/casilla")
@Tag(name = "casilla", description = "Casilla API")
public class CasillaController {
 
    CasillaService cs;

    @Autowired
    public CasillaController(CasillaService cs) {
        this.cs = cs;
    }

    @GetMapping
    public List<Casilla> findAll() {
        return cs.findAll();
    }

    @GetMapping("/{id}")
    public Casilla findById(int id) {
        Optional<Casilla> c = cs.findById(id);
        if(!c.isPresent()) {
            throw new ResourceNotFoundException("Casilla","id",id);
        }
        return c.get();
    }
}
