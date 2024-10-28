package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
@Service
public class PartidaCasillaService {

    private final PartidaCasillaRepository partidaCasillaRepository;

    @Autowired
    public PartidaCasillaService(PartidaCasillaRepository partidaCasillaRepository){
        
        this.partidaCasillaRepository = partidaCasillaRepository;
    }

    @Transactional
    public PartidaCasilla savePartidaCasilla(PartidaCasilla partidaCasilla) throws DataAccessException{
        partidaCasillaRepository.save(partidaCasilla);
        return partidaCasilla;
    }

    
	@Transactional()
	public PartidaCasilla findPartidaCasilla(Integer id) {
		return partidaCasillaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PartidaCasilla", "id", id));
	}	

    
	@Transactional()
	public Iterable<PartidaCasilla> findAll() {
		return partidaCasillaRepository.findAll();
	}
    
}

