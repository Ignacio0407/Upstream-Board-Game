package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
@Service
public class salmonMatchService {
    private final salmonMatchRepository salmonMatchRepository;

    @Autowired
    public salmonMatchService(salmonMatchRepository partidaSalmonRepository){
        
        this.salmonMatchRepository = partidaSalmonRepository;
    }

    @Transactional
    public salmonMatch savePartidaSalmon(salmonMatch partidaSalmon) throws DataAccessException{
        salmonMatchRepository.save(partidaSalmon);
        return partidaSalmon;
    }

    
	@Transactional()
	public salmonMatch findPartidaSalmon(Integer id) {
		return salmonMatchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PartidaSalmon", "id", id));
	}	

    
	@Transactional()
	public Iterable<salmonMatch> findAll() {
		return salmonMatchRepository.findAll();
	}
    
}
