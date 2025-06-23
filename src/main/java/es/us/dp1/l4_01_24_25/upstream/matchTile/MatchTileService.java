package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@Service
public class MatchTileService extends BaseServiceWithDTO<MatchTile, MatchTileDTO, Integer>{
    
    MatchTileRepository matchTileRepository;
    MatchTileMapper matchTileMapper;
    TileService tileService;
    MatchService matchService;

    @Autowired
    public MatchTileService(MatchTileRepository matchTileRepository, MatchTileMapper matchTileMapper, @Lazy TileService tileService, @Lazy MatchService matchService) {
        super(matchTileRepository, matchTileMapper);
        this.tileService = tileService;
        this.matchService = matchService;
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchId(Integer matchId) {
        return this.findList(matchTileRepository.findByMatchId(matchId));
    }

    @Transactional(readOnly = true)
    public List<MatchTileDTO> findByMatchIdAsDTO(Integer matchId) {
        return this.findListDTO(this.findByMatchId(matchId), matchTileMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchIdNoCoord(Integer matchId) {
        return this.findList(matchTileRepository.findByMatchIdNoCoord(matchId));
    }

    @Transactional(readOnly = true)
    public List<MatchTileDTO> findByMatchIdNoCoordAsDTO(Integer matchId) {
        return this.findListDTO(this.findByMatchIdNoCoord(matchId), matchTileMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public MatchTile findByCoordinate(Integer x, Integer y) {
        return matchTileRepository.findByCoordinate(x,y).orElseThrow(() -> new ResourceNotFoundException("Coordinate (" + x + y + ") not found"));
    }

    @Override
    @Transactional
    protected void updateEntityFields(MatchTile newMatchTile, MatchTile matchTileToUpdate) {
        matchTileToUpdate.setCapacity(newMatchTile.getCapacity());
        matchTileToUpdate.setOrientation(newMatchTile.getOrientation());
        matchTileToUpdate.setSalmonsNumber(newMatchTile.getSalmonsNumber());
        matchTileToUpdate.setCoordinate(newMatchTile.getCoordinate());
        matchTileToUpdate.setTile(newMatchTile.getTile());
        matchTileToUpdate.setMatch(newMatchTile.getMatch());
    }

    public Boolean validateTilePlacement(Integer round, Integer y) {
        int maxAllowedRow = round - 1;  
        return y <= maxAllowedRow;
    }
    
    public MatchTile eagleToWater(MatchTile toTravel, Match match) {
        MatchTile water = new MatchTile();
        Tile waterTile = tileService.findById(1);
        water.setId(toTravel.getId());
        water.setCapacity(toTravel.getCapacity());
        water.setOrientation(0);
        water.setSalmonsNumber(0);
        water.setCoordinate(toTravel.getCoordinate());
        water.setTile(waterTile);
        water.setMatch(match);
        matchTileRepository.save(water);
        return water;
    }

    public MatchTileDTO updateCoordinate(Integer id, Map<String, Integer> updates) {
        
        MatchTile matchTile = this.findById(id);

        if(updates.get("y") != 0){
            MatchTile matchTile2 = this.findByMatchId(matchTile.getMatch().getId()).stream().filter(mT -> mT.getCoordinate().y() == updates.get("y")-1 
            && mT.getCoordinate().x() == updates.get("x")).findFirst().orElse(null);
            if(matchTile2 == null){
                throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
            }
        }
    
        Boolean positionOccupied = this.findByMatchId(matchTile.getMatch().getId()).stream()
        .anyMatch(mT -> mT.getCoordinate() != null && mT.getCoordinate().x() == updates.get("x") && mT.getCoordinate().y() == updates.get("y"));
        if (positionOccupied)
            throw new IllegalStateException("Ya existe una MatchTile en las coordenadas especificadas.");
    
        if((matchTile.getMatch().getRound() == 0 && updates.get("y") > 3) || (matchTile.getMatch().getRound() == 1 && updates.get("y") > 4)
        || (matchTile.getMatch().getRound() == 6 && updates.get("x") == 1 && updates.get("y") == 5))
            throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID",id);
             
        if (updates.containsKey("x") && updates.containsKey("y")) {
            matchTile.setCoordinate(new Coordinate(updates.get("x"), updates.get("y")));
        }
        this.save(matchTile);
        return matchTileMapper.toDTO(matchTile);
    }

    public MatchTileDTO updateMatchTileRotation(Integer id, Integer rotation) {
        MatchTile matchTile = this.findById(id);
        matchTile.setOrientation(rotation);
        this.save(matchTile);
        return matchTileMapper.toDTO(matchTile);
    }

    public List<MatchTileDTO> createMultipleMatchTiles(Integer id) throws DataAccessException {
        Tile water = tileService.findById(1);
        Tile rock = tileService.findById(2);
        Tile heron = tileService.findById(3);
        Tile bear = tileService.findById(4);
        Tile eagle = tileService.findById(5);
        Tile jump = tileService.findById(6);
        Match match = matchService.findById(id);
        List<MatchTile> createdTiles = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(water);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(rock);
            matchTile.setMatch(match);
            if (match.getPlayersNumber() > 2) matchTile.setCapacity(match.getPlayersNumber()-1);
            else matchTile.setCapacity(2);
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(heron);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 3; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(bear);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(eagle);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        } 
        for (int i = 0; i < 4; i++) {
            MatchTile matchTile = new MatchTile();
            matchTile.setTile(jump);
            matchTile.setMatch(match);
            matchTile.setCapacity(match.getPlayersNumber());
            matchTile.setOrientation(0);
            matchTile.setCoordinate(null);
            matchTile.setSalmonsNumber(0);
            createdTiles.add(matchTile);
        }    
        Collections.shuffle(createdTiles);
        createdTiles.stream().forEach(mT -> this.save(mT));

        return matchTileMapper.toDTOList(createdTiles);
    }
}