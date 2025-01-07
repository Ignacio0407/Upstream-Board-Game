package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
@Service
public class salmonMatchService {
    private final salmonMatchRepository salmonMatchRepository;

    @Autowired
    public salmonMatchService(salmonMatchRepository partidaSalmonRepository){
        
        this.salmonMatchRepository = partidaSalmonRepository;
    }

    @Transactional
    public salmonMatch save(salmonMatch partidaSalmon) throws DataAccessException{
        salmonMatchRepository.save(partidaSalmon);
        return partidaSalmon;
    }

    
	@Transactional(readOnly = true)
	public salmonMatch getById(Integer id) {
		return salmonMatchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PartidaSalmon", "id", id));
	}	

    
	@Transactional(readOnly = true)
	public List<salmonMatch> getAllFromMatch(Integer matchId) {
		return salmonMatchRepository.findAllFromMatch(matchId);
	}
    
    @Transactional()
	public List<salmonMatch> getAllFromPlayer(Integer playerId) {
		return salmonMatchRepository.findAllFromPlayer(playerId);
	}

    @Transactional
    public void delete(Integer id) throws DataAccessException {
        salmonMatchRepository.deleteById(id);
    }
}
