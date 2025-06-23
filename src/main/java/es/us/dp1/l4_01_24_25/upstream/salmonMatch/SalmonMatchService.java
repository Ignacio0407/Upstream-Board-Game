package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

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
import es.us.dp1.l4_01_24_25.upstream.model.BaseService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
@Service
public class SalmonMatchService extends BaseService<SalmonMatch,Integer>{
    
    SalmonMatchRepository salmonMatchRepository;
    PlayerService playerService;
    SalmonService salmonService;
    MatchTileService matchTileService;
    MatchService matchService;

    @Autowired
    public SalmonMatchService(SalmonMatchRepository salmonMatchRepository, @Lazy PlayerService playerService, @Lazy SalmonService salmonService, @Lazy MatchTileService matchTileService, @Lazy MatchService matchService) {
        super(salmonMatchRepository);
        this.playerService = playerService;
        this.salmonService = salmonService;
        this.matchTileService = matchTileService;
        this.matchService = matchService;
    }
    
	@Transactional(readOnly = true)
	public List<SalmonMatch> findAllFromMatch(Integer matchId) {
		return this.findList(salmonMatchRepository.findAllFromMatch(matchId));
	}
    
    @Transactional()
	public List<SalmonMatch> findAllFromPlayer(Integer playerId) {
		return this.findList(salmonMatchRepository.findAllFromPlayer(playerId));
	}

    @Transactional
    public List<SalmonMatch> findAllFromPlayerInRiver(Integer playerId) {
        return this.findList(salmonMatchRepository.findAllFromPlayerInRiver(playerId));
    }

    @Transactional
    public List<SalmonMatch> findAllFromPlayerInSea(Integer playerId) {
        return this.findList(salmonMatchRepository.findAllFromPlayerInSea(playerId));
    }

    @Transactional(readOnly = true)
    public List<SalmonMatch> findFromGameInSpawn(Integer matchId) {
        return this.findList(salmonMatchRepository.findFromGameInSpawn(matchId));
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
        SalmonMatch sm = salmonMatchRepository.findById(salmonMatchId).get();
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

    private void throwExceptions(Coordinate myCoordinate, Coordinate newCoordinate, Player player) {
        // if (toTravel.getCapacity() < salmonsInToTravel + 1) throw new NoCapacityException("No queda hueco en esta casilla");
 
         if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("No puedes moverte en la misma casilla");
 
         if (player.getEnergy() <= 0) throw new InsufficientEnergyException("No hay suficiente energía para ese movimiento");
 
         if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
 
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
        TileType myCoordinateType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        Coordinate newCoordinate2 = newCoordinateToTravel(newCoordinate, x, y);
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).findFirst().orElse(null);
        if (toTravel2 == null) throw new NotValidMoveException("¡No se puede ir a la casilla a saltar o todavía no está puesta!");
        TileType toTravelType2 = toTravel2.getTile().getType();
        if (bearBoolean(myTile, toTravel2, myCoordinateType, toTravelType2, from, to)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        }
        return toTravel2;
    }

    private Coordinate newCoordinateToTravel(Coordinate newCoordinate, Integer x, Integer y) {
        return new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
    }

    private void herons(Player player, Match match, List<Player> players, Integer numPlayers) {
        List<SalmonMatch> salmonMatchesFromPlayer = this.findAllFromPlayerInRiver(player.getId());
        List<MatchTile> herons = matchService.findHeronWithCoordsFromGame(match.getId());
        for(MatchTile h : herons) {
            for(SalmonMatch s: salmonMatchesFromPlayer){
                if(s.getCoordinate().equals(h.getCoordinate())){
                    s.setSalmonsNumber(s.getSalmonsNumber()-1);
                    if(s.getSalmonsNumber()==0) {
                        this.delete(s.getId()); 
                        h.setSalmonsNumber(h.getSalmonsNumber()-1);
                        matchTileService.save(h);
                    }
                    else this.save(s);
                }
            }   
        }
        playerService.save(player);
        matchService.save(match);
    }

    private SalmonMatch processSalmonMovement(SalmonMatch salmonMatch, MatchTile toTravel, Player player, Match match, 
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
        return salmonMatch;
    }

    private Boolean bearBoolean(MatchTile myTile, MatchTile toTravel, TileType myCoordinateType, TileType toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals(TileType.BEAR) && from.contains(myTile.getOrientation()) ||
            toTravelType.equals(TileType.BEAR) && to.contains(toTravel.getOrientation());
    }

    public SalmonMatch updateCoordinate(Integer id, Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        SalmonMatch salmonMatch = this.findById(id);
        Player player = salmonMatch.getPlayer();
        Match match = salmonMatch.getMatch();
        List<Player> players = playerService.findPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNumber();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        List<MatchTile> matchTiles = matchTileService.findByMatchId(match.getId());
        MatchTile toTravel = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate)).findFirst().orElse(null);
        TileType toTravelType = toTravel.getTile().getType();
        Coordinate newCoordinate2 = null;
        MatchTile toTravel2 = null;
        Integer energyUsed = 1;

        throwExceptions(myCoordinate, newCoordinate, player);

        // Si salgo desde el mar.
        if (myCoordinate == null) {
            if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber()) ) {
                toTravel2 = tileFullNull(salmonMatch, matchTiles, newCoordinate);
                if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                    energyUsed = 3;
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                    if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = matchTileService.eagleToWater(toTravel2, match);
                        if (salmonMatch.getSalmonsNumber().equals(0)) 
                            this.delete(salmonMatch.getId());
                    }
                }
                else throw new NotValidMoveException("¡La casilla siguiente también está llena!");
            }
            else if(toTravelType.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel.getOrientation())) {
                salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                energyUsed = 2;
            } 
            else if(toTravelType.equals(TileType.JUMP) && List.of(0, 1, 5).contains(toTravel.getOrientation())) energyUsed = 2;
            else if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel, match);
            } 
        }

        // Si ya estoy en el tablero.
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            MatchTile myTile = matchTiles.stream().filter(mt -> mt.getCoordinate() != null && mt.getCoordinate().equals(myCoordinate)).findFirst().orElse(null);
            TileType myCoordinateType = myTile.getTile().getType();
            Coordinate distancia = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));

            if(distancia.y() < 0) throw new NotValidMoveException("Solo puedes moverte hacia delante"); 

            if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("Solo puedes moverte hacia delante");

            if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distancia.x()) == 1 && Math.abs(distancia.y()) == 1) throw new NotValidMoveException("Este movimiento no está permitido");    

            if (toTravel.getCapacity().equals(toTravel.getSalmonsNumber())) {
                    if (null != myCoordinate.x()) {
                        // Si estoy en el centro
                        if (newCoordinate.x().equals(myCoordinate.x())) { // Si subo hacia arriba
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 0, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                        
                        // Si voy al centro desde la izquierda
                        else if (newCoordinate.x() == myCoordinate.x() + 1) {
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, 1, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                        
                        // Si voy al centro desde la derecha
                        else if (newCoordinate.x() == myCoordinate.x() - 1) {
                            toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                            if (!toTravel2.getCapacity().equals(toTravel2.getSalmonsNumber())) {
                                energyUsed = 3;
                                newCoordinate2 = newCoordinateToTravel(newCoordinate, -1, 1);
                            }
                            else throw new NotValidMoveException("¡La casilla adyacente y la siguiente están llenas!");
                        }
                    else throw new NotValidMoveException("¡La casilla adyacente está llena y no se puede saltar a otra!");
                if (newCoordinate2 != null) {
                    if (player.getEnergy() < 3) 
                        throw new NotValidMoveException("¡Necesitas 3 puntos de energía para saltar una casilla llena!");
                    if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = matchTileService.eagleToWater(toTravel2, match);
                        if (salmonMatch.getSalmonsNumber().equals(0)) 
                            this.delete(salmonMatch.getId());
                    }
                    myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
                    matchTileService.save(myTile);
                    return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
                }
            }
    }

            if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = matchTileService.eagleToWater(toTravel, match);
        } 
            // SI SUBO
            if(newCoordinate.x().equals(myCoordinate.x()) && newCoordinate.y().equals(myCoordinate.y()+1)) {
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
            }

            // SI VOY A LA IZQUIERDA
            if(newCoordinate.x().equals(myCoordinate.x()-1)) {
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
            }

            // SI VOY A LA DERECHA
            if(newCoordinate.x().equals(myCoordinate.x()+1)) {
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
            }
            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            matchTileService.save(myTile);
        }

        else throw new NotValidMoveException("Solo puedes moverte de uno en uno"); 
        if (newCoordinate2 == null ) return processSalmonMovement(salmonMatch, toTravel, player, match, newCoordinate, energyUsed, players, numPlayers);
        else return processSalmonMovement(salmonMatch, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
    }

    public SalmonMatch create(Integer playerId) {
        SalmonMatch salmonMatch = new SalmonMatch();
        for (int i=0; i < 4; i++) {
            Player player = playerService.findById(playerId);
            Salmon salmon = salmonService.findAll().stream().filter(sal -> sal.getColor().equals(player.getColor())).findFirst().orElse(null);
            Match match = player.getMatch();
            Coordinate coordinate = new Coordinate(null, null);
            salmonMatch.setPlayer(player);
            salmonMatch.setSalmonsNumber(2);
            salmonMatch.setSpawningNumber(0);
            salmonMatch.setCoordinate(coordinate);
            salmonMatch.setSalmon(salmon);
            salmonMatch.setMatch(match);
        }
        return this.save(salmonMatch);
    }

    public SalmonMatch enterSpawn(Integer id) {
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

        if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            // Para subir 
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
                if(tileType.equals(TileType.BEAR) && List.of(3,4).contains(myTile.getOrientation())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(2,3,4).contains(myTile.getOrientation())) {
                    energyUsed = 2;    
                }
            }
            // Si vengo desde la izquierda
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) { 
                if(tileType.equals(TileType.BEAR) && List.of(4,5).contains(myTile.getOrientation())) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(3,4,5).contains(myTile.getOrientation())) {
                    energyUsed = 2;    
                }
            }

            // Vengo de la derecha
            else if(newCoordinate.x().equals(myCoordinate.x() - 1)){
                if(tileType.equals(TileType.BEAR) && List.of(2,3).contains(myTile.getOrientation())) {
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    energyUsed = 2;    
                }
                else if(tileType.equals(TileType.JUMP) && List.of(1,2,3).contains(myTile.getOrientation())) {
                    energyUsed = 2;    
                }
            }

            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            matchTileService.save(myTile);
            salmonMatch.setCoordinate(updateCoordinate);
            player.setEnergy(player.getEnergy() - energyUsed);
        }
    
        if(player.getEnergy() == 0) {
            Integer myOrder = player.getPlayerOrder();
            Player nextPlayer = players.stream().filter(pl -> pl.getPlayerOrder().equals((myOrder + 1)%numPlayers)).findFirst().orElse(null);
            match.setActualPlayer(nextPlayer);
            matchService.save(match);
        }
    
        playerService.save(player);
        if (salmonMatch.getSalmonsNumber() > 0) this.save(salmonMatch);
        else this.delete(salmonMatch.getId());
        return salmonMatch;

    }

    public List<SalmonMatch> findSalmonsInSpawnFromGame(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findSalmonsInSpawnFromGame'");
    }
}