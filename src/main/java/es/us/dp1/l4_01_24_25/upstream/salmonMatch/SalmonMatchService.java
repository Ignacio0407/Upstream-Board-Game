package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
@Service
public class SalmonMatchService extends BaseService<SalmonMatch,Integer>{
    SalmonMatchRepository salmonMatchRepository;

    @Autowired
    public SalmonMatchService(SalmonMatchRepository salmonMatchRepository){
        super(salmonMatchRepository);
    }	
    
	@Transactional(readOnly = true)
	public List<SalmonMatch> getAllFromMatch(Integer matchId) {
		List<SalmonMatch> res = salmonMatchRepository.findAllFromMatch(matchId);
        if(res == null) res = List.of();
        return res;
	}
    
    @Transactional()
	public List<SalmonMatch> getAllFromPlayer(Integer playerId) {
		return salmonMatchRepository.findAllFromPlayer(playerId);
	}

    @Transactional
    public List<SalmonMatch> getAllFromPlayerInRiver(Integer playerId) {
        List<SalmonMatch> res = salmonMatchRepository.findAllFromPlayerInRiver(playerId);
        if(res == null) res = List.of();
        return res;
    }

    @Transactional
    public List<SalmonMatch> getAllFromPlayerInSea(Integer playerId) {
        List<SalmonMatch> res = salmonMatchRepository.findAllFromPlayerInSea(playerId);
        if(res == null) res = List.of();
        return res;
    }

    @Transactional(readOnly = true)
    public List<SalmonMatch> getSalmonsInSpawnFromGame(Integer matchId) {
        List<SalmonMatch> sm = salmonMatchRepository.findFromGameInSpawn(matchId);
        if(sm == null) sm = List.of();
        return sm;
    }

    @Transactional
    public List<SalmonMatch> getSalmonsInSea(Integer matchId) {
        List<SalmonMatch> sm = salmonMatchRepository.findWithNoCoord(matchId);
        if(sm == null) sm = List.of();
        return sm;
    }

    @Transactional(readOnly = true)
    public List<SalmonMatch> getSalmonsFromPlayerInSpawn(Integer playerId){
        List<SalmonMatch> sm = salmonMatchRepository.findAllFromPlayerInSpawn(playerId);
        if(sm == null) sm = List.of();
        return sm;
    }

    @Transactional(readOnly = true)
    public Integer getPointsFromASalmonInSpawn(Integer salmonMatchId){
        SalmonMatch sm = salmonMatchRepository.findById(salmonMatchId).get();
        Integer puntuaje = 0;
        if(sm.getCoordinate().y() > 20){
         
        puntuaje = sm.getCoordinate().y() - 20;
    }
        return puntuaje;
    }
}