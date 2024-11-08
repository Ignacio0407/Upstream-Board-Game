package es.us.dp1.l4_01_24_25.upstream.casilla;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

public class TipoCasillaService {
    
    private final TipoCasillaRepository tipoCasillaRepository;

    @Autowired
    public TipoCasillaService(TipoCasillaRepository tipoCasillaRepository){
        
        this.tipoCasillaRepository = tipoCasillaRepository;
    }

    @Transactional
    public TipoCasilla findById(Integer id) throws ResourceNotFoundException {
        return tipoCasillaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TipoCasilla", "id", id));
    }

}
