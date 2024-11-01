package es.us.dp1.l4_01_24_25.upstream.partidaSalmon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
@Service
public class PartidaSalmonService {
    private final PartidaSalmonRepository partidaSalmonRepository;

    @Autowired
    public PartidaSalmonService(PartidaSalmonRepository partidaSalmonRepository){
        
        this.partidaSalmonRepository = partidaSalmonRepository;
    }

    @Transactional
    public PartidaSalmon savePartidaSalmon(PartidaSalmon partidaSalmon) throws DataAccessException{
        partidaSalmonRepository.save(partidaSalmon);
        return partidaSalmon;
    }

    
	@Transactional()
	public PartidaSalmon findPartidaSalmon(Integer id) {
		return partidaSalmonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PartidaSalmon", "id", id));
	}	

    
	@Transactional()
	public Iterable<PartidaSalmon> findAll() {
		return partidaSalmonRepository.findAll();
	}
    
}
