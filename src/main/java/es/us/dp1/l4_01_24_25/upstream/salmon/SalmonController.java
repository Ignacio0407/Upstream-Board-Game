package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/v1/salmons")
@Tag(name="Salmon", description="Salmon API")
public class SalmonController {
    SalmonService salmonService;

    @Autowired
    public SalmonController(SalmonService salmonService) {
        this.salmonService = salmonService;
    }

    @GetMapping
    public ResponseEntity<List<Salmon>> findAll() {
        List<Salmon> res = (List<Salmon>) salmonService.findAll();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Salmon findById(@PathVariable("id") int id) {
        Optional<Salmon> s = salmonService.findById(id);
        if(!s.isPresent()) {
            throw new ResourceNotFoundException("Salmon","id",id);
        }
        return s.get();
    }
}
