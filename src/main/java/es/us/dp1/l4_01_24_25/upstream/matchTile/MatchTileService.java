package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;



@Service
public class MatchTileService {
    
    MatchTileRepository matchTileRepository;
    TileService tileService;

    @Autowired
    public MatchTileService(MatchTileRepository matchTileRepository, TileService tileService) {
        this.matchTileRepository = matchTileRepository;
        this.tileService = tileService;
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

    @Transactional(readOnly = true)
    public MatchTile findByCoordinate(Integer x, Integer y) {
        return matchTileRepository.findByCoordinate(x,y);
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
    
    public MatchTile eagleToWater(MatchTile toTravel, Match match) {
        MatchTile agua = new MatchTile();
        Tile aguaTile = tileService.findById(1).orElse(null);
        agua.setTile(aguaTile);
        agua.getTile().getType().setType("AGUA");
        agua.setMatch(match);
        agua.setCapacity(toTravel.getCapacity());
        agua.setOrientation(0);
        agua.setCoordinate(toTravel.getCoordinate());
        //matchTile.setJumpingSides(new ArrayList<>());
        agua.setSalmonsNumber(0);
        agua.setTimesHasEaten(0);
        return agua;
    }


}
