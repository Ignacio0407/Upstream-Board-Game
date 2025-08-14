package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NoCapacityException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.OnlyMovingForwardException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@Service
public class SalmonMatchService extends BaseServiceWithDTO<SalmonMatch, SalmonMatchDTO, Integer>{
    
    SalmonMatchRepository salmonMatchRepository;
    SalmonMatchMapper salmonMatchMapper;
    PlayerService playerService;
    SalmonService salmonService;
    MatchTileService matchTileService;
    MatchService matchService;

    @Autowired
    public SalmonMatchService(SalmonMatchRepository salmonMatchRepository, SalmonMatchMapper salmonMatchMapper, @Lazy PlayerService playerService, @Lazy SalmonService salmonService, @Lazy MatchTileService matchTileService, @Lazy MatchService matchService) {
        super(salmonMatchRepository, salmonMatchMapper);
        this.salmonMatchRepository = salmonMatchRepository;
        this.salmonMatchMapper = salmonMatchMapper;
        this.playerService = playerService;
        this.salmonService = salmonService;
        this.matchTileService = matchTileService;
        this.matchService = matchService;
    }
    
	@Transactional(readOnly = true)
	public List<SalmonMatch> findAllFromMatch(Integer matchId) {
		return this.findList(salmonMatchRepository.findAllFromMatch(matchId));
	}

    @Transactional(readOnly = true)
	public List<SalmonMatchDTO> findAllFromMatchDTO(Integer matchId) {
		return this.findList(salmonMatchRepository.findAllFromMatchAsDTO(matchId));
	}
    
    @Transactional()
	public List<SalmonMatch> findAllFromPlayer(Integer playerId) {
		return this.findList(salmonMatchRepository.findAllFromPlayer(playerId));
	}

    @Transactional()
	public List<SalmonMatchDTO> findAllFromPlayerDTO(Integer playerId) {
		return this.findList(salmonMatchRepository.findAllFromPlayerAsDTO(playerId));
	}

    @Transactional(readOnly = true)
    public List<SalmonMatch> findFromGameInSpawn(Integer matchId) {
        return this.findList(salmonMatchRepository.findFromGameInSpawn(matchId));
    }

    @Transactional(readOnly = true)
    public List<SalmonMatchDTO> findFromGameInSpawnDTO(Integer matchId) {
        return this.findList(salmonMatchRepository.findFromGameInSpawnAsDTO(matchId));
    }

    @Transactional
    public List<SalmonMatch> findAllFromPlayerInRiver(Integer playerId) {
        return this.findList(salmonMatchRepository.findAllFromPlayerInRiver(playerId));
    }

    @Transactional
    public List<SalmonMatch> findAllFromPlayerInSea(Integer playerId) {
        return this.findList(salmonMatchRepository.findAllFromPlayerInSea(playerId));
    }

    @Transactional
    public List<SalmonMatch> findSalmonsInSea(Integer matchId) {
        return this.findList(salmonMatchRepository.findByMatchIdNoCoord(matchId));
    }

    @Transactional(readOnly = true)
    public List<SalmonMatch> findSalmonsFromPlayerInSpawn(Integer playerId){
        return this.findList(salmonMatchRepository.findAllFromPlayerInSpawn(playerId));
    }

    @Transactional(readOnly = true)
    public List<SalmonMatch> findByMatchIdNoCoord(Integer matchId){
        return this.findList(salmonMatchRepository.findByMatchIdNoCoord(matchId));
    }

    @Transactional(readOnly = true)
    public Integer findPointsFromASalmonInSpawn(Integer salmonMatchId){
        SalmonMatch sm = this.findById(salmonMatchId);
        return sm.getCoordinate().y() > 20 ? sm.getCoordinate().y() - 20 : 0;
    }

    @Override
    @Transactional
    protected void updateEntityFields (SalmonMatch newSalmonMatch, SalmonMatch salmonMatchToUpdate) {
        salmonMatchToUpdate.setPlayer(newSalmonMatch.getPlayer());
        salmonMatchToUpdate.setSalmonsNumber(newSalmonMatch.getSalmonsNumber());
        salmonMatchToUpdate.setSpawningNumber(newSalmonMatch.getSpawningNumber());
        salmonMatchToUpdate.setCoordinate(newSalmonMatch.getCoordinate());
        salmonMatchToUpdate.setSalmon(newSalmonMatch.getSalmon());
        salmonMatchToUpdate.setMatch(newSalmonMatch.getMatch());
    }

    public void setSalmonImage (SalmonMatch salmonMatch) {
        if (!salmonMatch.getSalmon().getImage().contains("1") && salmonMatch.getSalmonsNumber() == 1)
            salmonMatch.setSalmon(salmonService.findById(salmonMatch.getSalmon().getId()+5));
    }

    private void throwExceptionsInitial(Coordinate myCoordinate, Coordinate newCoordinate, Player player) {
         if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("You cannot go to where yo already are");
         if (player.getEnergy() <= 0) throw new InsufficientEnergyException("Not enough energy for that move");
         if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("You can only advance from 1 to 1"); 
    }

    private void throwExceptionsGrid (Coordinate distance, Coordinate myCoordinate, Coordinate newCoordinate) {
        if(distance.y() < 0) throw new NotValidMoveException("You can only move forward"); 
        if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("You can only move forward");
        if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distance.x()) == 1 && Math.abs(distance.y()) == 1) throw new NotValidMoveException("Not allowed movement");    
    }

    private MatchTile tileFullNull(SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate) {
        Coordinate newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).toList().get(0);
        TileType toTravelType2 = toTravel2.getTile().getType();
        if(toTravelType2.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel2.getOrientation())) 
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        return toTravel2;
    }

    private MatchTile handleTileFull(Coordinate newCoordinate, SalmonMatch salmonMatch, MatchTile myTile, 
        TileType myCoordinateType, MatchTile toTravel, TileType toTravelType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        Coordinate newCoordinate2 = newCoordinateToTravel(newCoordinate, x, y);
        MatchTile toTravel2 = matchTileService.findMyTile(matchTiles, newCoordinate2);
        if (toTravel2 == null) throw new NotValidMoveException("You cannot go to the tile or it has not still been placed!");
        TileType toTravelType2 = toTravel2.getTile().getType();
        if (bearBoolean(myTile, toTravel2, myCoordinateType, toTravelType2, from, to) || bearBoolean(myTile, toTravel, myCoordinateType, toTravelType, from, to)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        }
        return toTravel2;
    }

    private Coordinate newCoordinateToTravel(Coordinate newCoordinate, Integer x, Integer y) {
        return new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
    }

    private void herons(Player player, Match match, List<Player> players, Integer numPlayers) {
        List<SalmonMatch> salmonMatchesFromPlayer = this.findAllFromPlayerInRiver(player.getId());
        List<MatchTile> herons = matchTileService.findHeronWithCoordsFromGame(match.getId());
        for(MatchTile heron : herons) {
            for(SalmonMatch salmonMatch: salmonMatchesFromPlayer) {
                if(salmonMatch.getCoordinate().equals(heron.getCoordinate())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    this.setSalmonImage(salmonMatch);
                    if(salmonMatch.getSalmonsNumber()==0) {
                        this.delete(salmonMatch.getId()); 
                        heron.setSalmonsNumber(heron.getSalmonsNumber()-1);
                        matchTileService.save(heron);
                    }
                }
                this.saveAll(salmonMatchesFromPlayer);
            }   
        }
        matchService.save(match);
    }

    private SalmonMatchDTO processSalmonMovement(SalmonMatch salmonMatch, MatchTile toTravel, Player player, Match match, 
            Coordinate newCoordinate, Integer energyUsed, List<Player> players, Integer numPlayers) {
        salmonMatch.setCoordinate(newCoordinate);
        toTravel.setSalmonsNumber(toTravel.getSalmonsNumber() + 1);
        matchTileService.save(toTravel);
        player.setEnergy(player.getEnergy() - energyUsed);
        if (player.getEnergy() == 0) {
            herons(player, match, players, numPlayers);
        }
        playerService.save(player);
        if (salmonMatch.getSalmonsNumber() > 0) {
            this.save(salmonMatch);
        } else {
            this.delete(salmonMatch.getId());
        }
        return salmonMatchMapper.toDTO(salmonMatch);
    }

    private Boolean bearBoolean(MatchTile myTile, MatchTile toTravel, TileType myCoordinateType, TileType toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals(TileType.BEAR) && from.contains(myTile.getOrientation()) ||
            toTravelType.equals(TileType.BEAR) && to.contains(toTravel.getOrientation());
    }

    private record CoordinateInteger(Coordinate coordinate, MatchTile toTravel, Integer energyUsed) {}

    private CoordinateInteger fromSea (MatchTile toTravel, MatchTile toTravel2, SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate, Integer energyUsed, Coordinate newCoordinate2, TileType toTravelType) {
        if (toTravel.isFull()) {
                toTravel2 = tileFullNull(salmonMatch, matchTiles, newCoordinate);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                    if (toTravelType.equals(TileType.BEAR)) salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = matchTileService.eagleToWater(toTravel2);
                    }
                }
                else throw new NotValidMoveException("The next tile is also full!");
        }
        else if (toTravelType.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel.getOrientation())) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            energyUsed = 2;
        } 
        else if(toTravelType.equals(TileType.JUMP) && List.of(0, 1, 5).contains(toTravel.getOrientation())) energyUsed = 2;
        else if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel);
        }
        return new CoordinateInteger(newCoordinate2, toTravel2, energyUsed); 
    }

    private Integer above (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch) {
        Boolean cond1 = toTravelType.equals(TileType.BEAR) && List.of(0,1).contains(toTravel.getOrientation());
        Boolean cond2 = toTravelType.equals(TileType.JUMP) && List.of(0,1,5).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(3,4).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(2,3,4).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        else {
            if(cond1 || cond2) energyUsed = 2;
                salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
        }
        return energyUsed;
    }

    private Integer left (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch) {
        Boolean cond1 = toTravelType.equals(TileType.BEAR) && List.of(0,5).contains(toTravel.getOrientation());
        Boolean cond2 = toTravelType.equals(TileType.JUMP) && List.of(0,4,5).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(2,3).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(1,2,3).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        else {
            if(cond1 || cond2) energyUsed = 2;
                salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
        }
        return energyUsed;
    }

    private Integer right (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch) {
        Boolean cond1 = toTravelType.equals(TileType.BEAR) && List.of(1,2).contains(toTravel.getOrientation());
        Boolean cond2 = toTravelType.equals(TileType.JUMP) && List.of(0,1,2).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(4,5).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-2:cond2?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(3,4,5).contains(myTile.getOrientation())) {
                energyUsed = 2;
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(cond1 || cond2) energyUsed = 2;
                    salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        else {
            if(cond1 || cond2) energyUsed = 2;
                salmonMatch.setSalmonsNumber(cond1?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
        }
        return energyUsed;
    }

    private SalmonMatchDTO fromGridFull (List<MatchTile> matchTiles, Coordinate myCoordinate, Coordinate newCoordinate, 
    MatchTile toTravel, MatchTile toTravel2, SalmonMatch salmonMatch, Integer energyUsed, Coordinate newCoordinate2, 
    Player player, Match match, List<Player> players, Integer numPlayers, TileType toTravelType, MatchTile myTile, TileType myCoordinateType) {
        if (null != myCoordinate.x()) {
            // Si estoy en el centro
            if (newCoordinate.x().equals(myCoordinate.x())) { // Si subo hacia arriba
                toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, toTravel, toTravelType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                }
                else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
            }
            // Si voy al centro desde la izquierda
            else if (newCoordinate.x() == myCoordinate.x() + 1) {
                toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = newCoordinateToTravel(newCoordinate, 1, 1);
                }
                else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
            }
            // Si voy al centro desde la derecha
            else if (newCoordinate.x() == myCoordinate.x() - 1) {
                toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = newCoordinateToTravel(newCoordinate, -1, 1);
                }
                else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
            }
            else throw new NotValidMoveException("¡La casilla adyacente está llena y no se puede saltar a otra!");
            if (newCoordinate2 != null) {
                if (player.getEnergy() < 3) 
                    throw new NotValidMoveException("You need 3 energy points to jump a full tile!");
                if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    toTravel2 = matchTileService.eagleToWater(toTravel2);
                    if (salmonMatch.getSalmonsNumber().equals(0)) 
                        this.delete(salmonMatch.getId());
                }
                myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
                matchTileService.save(myTile);
                this.setSalmonImage(salmonMatch);
                return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
            }
        }
        throw new IllegalStateException("Move from full tile couldn't be calculated");
    }

    private Integer fromGrid (Coordinate myCoordinate,  MatchTile myTile, TileType myCoordinateType, Coordinate newCoordinate, 
    MatchTile toTravel,  TileType toTravelType, SalmonMatch salmonMatch, Integer energyUsed) {
        Coordinate distance = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
        this.throwExceptionsGrid(distance, myCoordinate, newCoordinate);

        if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel);
        } 
        // SI SUBO
        if(newCoordinate.x().equals(myCoordinate.x()) && newCoordinate.y().equals(myCoordinate.y()+1))
            energyUsed = this.above(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch);
        // SI VOY A LA IZQUIERDA
        if(newCoordinate.x().equals(myCoordinate.x()-1))
            energyUsed = this.left(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch);
        // SI VOY A LA DERECHA
        if(newCoordinate.x().equals(myCoordinate.x()+1)) {
            energyUsed = this.right(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch);
        }
        myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
        matchTileService.save(myTile);
        return energyUsed;
    }

    @Transactional
    public SalmonMatchDTO updateCoordinate(Integer id, Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        SalmonMatch salmonMatch = this.findById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.findPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNumber();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        List<MatchTile> matchTiles = matchTileService.findByMatchId(match.getId());
        MatchTile toTravel = matchTileService.findMyTile(matchTiles, newCoordinate);
        TileType toTravelType = toTravel.getTile().getType();
        Coordinate newCoordinate2 = null;
        MatchTile toTravel2 = null;
        Integer energyUsed = 1;
        this.throwExceptionsInitial(myCoordinate, newCoordinate, player);

        if (myCoordinate == null) {
            CoordinateInteger aux = this.fromSea(toTravel, toTravel2, salmonMatch, matchTiles, newCoordinate, energyUsed, newCoordinate2, toTravelType);
            newCoordinate2 = aux.coordinate();
            energyUsed = aux.energyUsed();
            toTravel2 = aux.toTravel();
        }
        // I'm in grid
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            MatchTile myTile = matchTileService.findMyTile(matchTiles, myCoordinate);
            TileType myCoordinateType = myTile.getTile().getType();;
            if (toTravel.isFull())
                return this.fromGridFull(matchTiles, myCoordinate, newCoordinate, toTravel, toTravel2, salmonMatch, energyUsed, newCoordinate2, player, match, players, numPlayers, toTravelType, myTile, myCoordinateType);
            else
                energyUsed = this.fromGrid(myCoordinate, myTile, myCoordinateType, newCoordinate, toTravel, toTravelType, salmonMatch, energyUsed);
        }
        else throw new NotValidMoveException("You can only move from 1 to 1");
        setSalmonImage(salmonMatch); 
        if (newCoordinate2 == null )  return processSalmonMovement(salmonMatch, toTravel, player, match, newCoordinate, energyUsed, players, numPlayers);
        else return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
    }

    public List<SalmonMatchDTO> create(Integer playerId) {
        List<SalmonMatch> salmonMatches = new ArrayList<>();
        Player player = playerService.findById(playerId);
        Match match = player.getMatch();
        for (int i=0; i < 4; i++) {
            SalmonMatch salmonMatch = new SalmonMatch();
            Salmon salmon = salmonService.findFirstByColor(player.getColor());
            Coordinate coordinate = new Coordinate(null, null);
            salmonMatch.setPlayer(player);
            salmonMatch.setSalmonsNumber(2);
            salmonMatch.setSpawningNumber(0);
            salmonMatch.setCoordinate(coordinate);
            salmonMatch.setSalmon(salmon);
            salmonMatch.setMatch(match);
            salmonMatches.add(salmonMatch);
        }
        this.saveAll(salmonMatches);
        return salmonMatchMapper.toDTOList(salmonMatches);
    }

    private void enterSpawnMovement (Coordinate myCoordinate, Coordinate newCoordinate, TileType tileType, MatchTile myTile, 
    SalmonMatch salmonMatch, Coordinate updateCoordinate, Integer energyUsed, Player player) {
        if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            // Para subir 
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
                if(tileType.equals(TileType.BEAR) && List.of(3,4).contains(myTile.getOrientation())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(2,3,4).contains(myTile.getOrientation()))
                    energyUsed = 2;    
            }
            // Si vengo desde la izquierda
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) { 
                if(tileType.equals(TileType.BEAR) && List.of(4,5).contains(myTile.getOrientation())) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(3,4,5).contains(myTile.getOrientation()))
                    energyUsed = 2;    
            }
            // Vengo de la derecha
            else if(newCoordinate.x().equals(myCoordinate.x() - 1)){
                if(tileType.equals(TileType.BEAR) && List.of(2,3).contains(myTile.getOrientation())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(1,2,3).contains(myTile.getOrientation()))
                    energyUsed = 2;    
            }

            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            matchTileService.save(myTile);
            salmonMatch.setCoordinate(updateCoordinate);
            player.setEnergy(player.getEnergy() - energyUsed);
        }
    }

    public SalmonMatchDTO enterSpawn(Integer id) {
        SalmonMatch salmonMatch = this.findById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.findPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNumber();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(1,5);
        Coordinate updateCoordinate = new Coordinate(1, 21);
        Integer energyUsed = 1;
        MatchTile myTile = matchTileService.findByMatchId(match.getId()).stream()
            .filter(m -> salmonMatch.getCoordinate().equals(m.getCoordinate())).findFirst().orElse(null);
        TileType tileType = myTile.getTile().getType();

        this.enterSpawnMovement(myCoordinate, newCoordinate, tileType, myTile, salmonMatch, updateCoordinate, energyUsed, player);
    
        if(player.getEnergy() == 0) {
            Integer myOrder = player.getPlayerOrder();
            Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).findFirst().orElse(null);
            match.setActualPlayer(nextPlayer);
            matchService.save(match);
        }
    
        playerService.save(player);
        if (salmonMatch.getSalmonsNumber() > 0) this.save(salmonMatch);
        else this.delete(salmonMatch.getId());
        return salmonMatchMapper.toDTO(salmonMatch);
    }

}