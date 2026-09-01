package es.us.dp1.l4_01_24_25.upstream.matchtile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ConflictException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.matchtile.dto.MTCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.matchtile.dto.MatchTileDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Service
public class MatchTileService extends BaseServiceWithDTO<MatchTile, MatchTileDTO, Integer>{
    
    private static final String ORIENTATION_KEY = "orientation";

    MatchTileRepository matchTileRepository;
    MatchTileMapper matchTileMapper;
    TileService tileService;
    MatchService matchService;
    PlayerService playerService;
    UserService userService;
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MatchTileService(MatchTileRepository matchTileRepository, MatchTileMapper matchTileMapper, @Lazy TileService tileService, @Lazy MatchService matchService, PlayerService playerService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        super(matchTileRepository, matchTileMapper);
        this.matchTileRepository = matchTileRepository;
        this.matchTileMapper = matchTileMapper;
        this.tileService = tileService;
        this.matchService = matchService;
        this.playerService = playerService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<MatchTile> findByMatchId(Integer matchId) {
        return this.matchTileRepository.findByMatchId(matchId);
    }

    public List<MatchTileDTO> findByMatchIdAsDTO(Integer matchId) {
        return this.matchTileRepository.findByMatchIdAsDTO(matchId);
    }

    public List<MatchTile> findByMatchIdNoCoord(Integer matchId) {
        return this.matchTileRepository.findByMatchIdAndCoordinateIsNull(matchId);
    }

    public List<MatchTileDTO> findByMatchIdNoCoordAsDTO(Integer matchId) {
        return this.matchTileRepository.findByMatchIdAndCoordinateIsNullAsDTO(matchId);
    }

    public MatchTile findByCoordinate(Integer x, Integer y) {
        return this.matchTileRepository.findByCoordinate(x,y).orElseThrow(() -> new ResourceNotFoundException("Coordinate (" + x + y + ") not found"));
    }

    public List<MatchTile> findHeronWithCoordsFromGame(Integer matchId){
        return this.matchTileRepository.findHeronWithCoordFromGame(matchId);
    }

    public List<MTCoordinateDTO> findByMatchIdNoCoordAsMTCoordinateDTO (Integer matchId) {
        return this.matchTileRepository.findByMatchIdNoCoordAsMTCoordinateDTO(matchId);
    }

    public List<MTCoordinateDTO> findByMatchIdAsMTCoordinateDTO (Integer matchId) {
        return this.matchTileRepository.findByMatchIdAsMTCoordinateDTO(matchId);
    }

    @Override
    @Transactional
    protected MatchTile updateEntityFields(MatchTile newMatchTile, MatchTile matchTileToUpdate) {
        matchTileToUpdate.setCapacity(newMatchTile.getCapacity());
        matchTileToUpdate.setOrientation(newMatchTile.getOrientation());
        matchTileToUpdate.setSalmonsNumber(newMatchTile.getSalmonsNumber());
        matchTileToUpdate.setCoordinate(newMatchTile.getCoordinate());
        matchTileToUpdate.setTile(newMatchTile.getTile());
        matchTileToUpdate.setMatch(newMatchTile.getMatch());
        return matchTileToUpdate;
    }

    public MatchTileDTO toDTO(MatchTile matchTile) {
        if (matchTile == null) {
            return null;
        }
        return this.matchTileMapper.toDTO(matchTile);
    }

    public List<MatchTileDTO> toDTOList(List<MatchTile> matchTiles) {
        return this.matchTileMapper.toDTOList(matchTiles);
    }

    public Boolean validateTilePlacement(Integer round, Integer y) {
        int maxAllowedRow = round - 1;  
        return y <= maxAllowedRow;
    }
    
    public MatchTile eagleToWater(MatchTile toTravel) {
        Tile waterTile = tileService.findById(1);
        toTravel.setTile(waterTile);
        return this.save(toTravel);
    }

    public MatchTile findMyTile (List<MatchTile> matchTiles, Coordinate myCoordinate) {
        return matchTiles.stream().filter(mt -> mt.getCoordinate() != null && mt.getCoordinate().equals(myCoordinate)).findFirst().orElse(null);
    }

    @Transactional
    public void updateCoordinate(Integer id, Map<String, Integer> updates) throws NotValidMoveException {
        MatchTile matchTile = this.findById(id);
        Match match = matchTile.getMatch();
        Player myPlayer = this.playerService.findMyPlayer(match.getId());
        if (!match.getActualPlayer().getId().equals(myPlayer.getId())) {
            throw new NotValidMoveException("It is not your turn!");
        }
        if (updates.containsKey(ORIENTATION_KEY) && updates.get(ORIENTATION_KEY) != null) {
            matchTile.setOrientation(updates.get(ORIENTATION_KEY));
        }
        List<MatchTile> matchTiles = this.findByMatchId(matchTile.getMatch().getId());
        if (!Integer.valueOf(0).equals(updates.get("y"))) {
            boolean hasSupport = matchTiles.stream().filter(mT -> mT.getCoordinate() != null)
                .anyMatch(mT -> mT.getCoordinate().y().equals(updates.get("y") - 1) && mT.getCoordinate().x().equals(updates.get("x")));

            if (!hasSupport && !updates.get("y").equals(1)) {
                throw new ResourceNotFoundException("No se puede actualizar el MatchTile en esta ronda", "ID", id);
            }
        }
        boolean positionOccupied = matchTiles.stream()
        .anyMatch(mT -> mT.getCoordinate() != null && mT.getCoordinate().x().equals(updates.get("x")) && mT.getCoordinate().y().equals(updates.get("y")));
        if (positionOccupied)
            throw new ConflictException("Ya existe una MatchTile en las coordenadas especificadas.");
    
        if((matchTile.getMatch().getRound().equals(0) && updates.get("y") > 3) || (matchTile.getMatch().getRound().equals(1) && updates.get("y") > 4)
        || (matchTile.getMatch().getRound().equals(6) && updates.get("x").equals(1) && updates.get("y").equals(5)))
            throw new ConflictException("No se puede actualizar el MatchTile en esta ronda");
             
        if (updates.containsKey("x") && updates.containsKey("y")) {
            matchTile.setCoordinate(new Coordinate(updates.get("x"), updates.get("y")));
        }
        this.save(matchTile);
        MatchTileDTO updatedTile = this.matchTileMapper.toDTO(matchTile);
        this.messagingTemplate.convertAndSend("/topic/tiles/place/" + updatedTile.getMatchId(), updatedTile);
        this.playerService.notifyPlayersUpdate(updatedTile.getMatchId());
        this.matchService.notifyMatchUpdate(match);
    }

    public void broadcastMatchTileRotation(Integer id, Integer rotation) {
        MatchTile matchTile = this.findById(id);
        Match match = matchTile.getMatch();
        Player myPlayer = this.playerService.findMyPlayer(match.getId());
        if (!match.getActualPlayer().getId().equals(myPlayer.getId())) {
            throw new NotValidMoveException("It is not your turn!");
        }
        matchTile.setOrientation(rotation);
        MatchTileDTO updatedTile = this.matchTileMapper.toDTO(matchTile);
        this.messagingTemplate.convertAndSend("/topic/tiles/rotation/" + updatedTile.getMatchId(), updatedTile);
    }

    private void createMatchTile(MatchTile matchTile, Tile tile, Match match, List<MatchTile> createdTiles) {
        matchTile.setTile(tile);
        matchTile.setMatch(match);
        if (tile.getType().equals(TileType.ROCK)) {
            if (match.getPlayersNumber() > 2) matchTile.setCapacity(match.getPlayersNumber()-1);
            else matchTile.setCapacity(2); 
        }
        else matchTile.setCapacity(match.getPlayersNumber());
        matchTile.setOrientation(0);
        matchTile.setCoordinate(null);
        matchTile.setSalmonsNumber(0);
        createdTiles.add(matchTile);
    }

    public void createMultipleMatchTiles(Integer id) throws DataAccessException {
        Match match = this.matchService.findById(id);
        Tile water = this.tileService.findById(1);
        Tile rock = this.tileService.findById(2);
        Tile heron = this.tileService.findById(3);
        Tile bear = this.tileService.findById(4);
        Tile eagle = this.tileService.findById(5);
        Tile jump = this.tileService.findById(6);
        List<MatchTile> createdTiles = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, water, match, createdTiles);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, rock, match, createdTiles);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, heron, match, createdTiles);
        }
        for (int i = 0; i < 3; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, bear, match, createdTiles);
        }
        for (int i = 0; i < 5; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, eagle, match, createdTiles);
        } 
        for (int i = 0; i < 4; i++) {
            MatchTile matchTile = new MatchTile();
            this.createMatchTile(matchTile, jump, match, createdTiles);
        }    
        Collections.shuffle(createdTiles);
        this.saveAll(createdTiles);
        this.matchService.notifyMatchStarts(match.getId());
    }

}