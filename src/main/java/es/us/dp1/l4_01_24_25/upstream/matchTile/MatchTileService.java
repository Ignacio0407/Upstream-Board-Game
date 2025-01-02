package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class MatchTileService {
    
    MatchTileRepository matchTileRepository;

    @Autowired
    public MatchTileService(MatchTileRepository matchTileRepository) {
        this.matchTileRepository = matchTileRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findAll() {
        return matchTileRepository.findAll();
    }

    @Transactional
    public MatchTile save(MatchTile matchTile) {
        matchTileRepository.save(matchTile);
        return matchTile;
    }

    @Transactional(readOnly = true)
    public MatchTile findById(Integer id) {
        return matchTileRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchId(Integer matchId) {
        return matchTileRepository.findByMatchId(matchId);
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchIdNoCoord(Integer matchId) {
        return matchTileRepository.findWithNoCoord(matchId);
    }

    @Transactional
    public void deleteMatchTile(Integer id) {
        MatchTile toDelete = findById(id);
        this.matchTileRepository.delete(toDelete);
    }

    public Boolean validateTilePlacement(Integer round, Integer y) {
        int maxAllowedRow = round - 1;  
        return y <= maxAllowedRow;
    }
    


}
