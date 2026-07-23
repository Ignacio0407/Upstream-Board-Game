package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.exceptions.InsufficientEnergyException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.model.BaseServiceWithDTO;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.salmon.SalmonService;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SMCoordinateDTO;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO.SalmonMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;

@Service
public class SalmonMatchService extends BaseServiceWithDTO<SalmonMatch, SalmonMatchDTO, Integer>{
    
    SalmonMatchRepository salmonMatchRepository;
    public SalmonMatchMapper salmonMatchMapper;
    PlayerService playerService;
    SalmonService salmonService;
    MatchTileService matchTileService;
    MatchService matchService;
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SalmonMatchService(SalmonMatchRepository salmonMatchRepository, SalmonMatchMapper salmonMatchMapper, @Lazy PlayerService playerService, @Lazy SalmonService salmonService, @Lazy MatchTileService matchTileService, @Lazy MatchService matchService, SimpMessagingTemplate messagingTemplate) {
        super(salmonMatchRepository, salmonMatchMapper);
        this.salmonMatchRepository = salmonMatchRepository;
        this.salmonMatchMapper = salmonMatchMapper;
        this.playerService = playerService;
        this.salmonService = salmonService;
        this.matchTileService = matchTileService;
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }

	public List<SalmonMatch> findAllFromMatch(Integer matchId) {
		return this.salmonMatchRepository.findByMatchId(matchId);
	}

	public List<SalmonMatchDTO> findAllFromMatchDTO(Integer matchId) {
		return this.salmonMatchRepository.findByMatchIdAsDTO(matchId);
	}

	public List<SalmonMatch> findAllFromPlayer(Integer playerId) {
		return this.salmonMatchRepository.findByPlayerId(playerId);
	}

	public List<SalmonMatchDTO> findAllFromPlayerDTO(Integer playerId) {
		return this.salmonMatchRepository.findByPlayerIdAsDTO(playerId);
	}

    public List<SalmonMatch> findFromGameInSpawn(Integer matchId) {
        return this.salmonMatchRepository.findFromGameInSpawn(matchId);
    }

    public List<SalmonMatchDTO> findFromGameInSpawnDTO(Integer matchId) {
        return this.salmonMatchRepository.findFromGameInSpawnAsDTO(matchId);
    }

    public List<SalmonMatch> findAllFromPlayerInRiver(Integer playerId) {
        return this.salmonMatchRepository.findAllFromPlayerInRiver(playerId);
    }

    public List<SalmonMatch> findAllFromPlayerInSea(Integer playerId) {
        return this.salmonMatchRepository.findByPlayerIdAndCoordinateIsNull(playerId);
    }

    public List<SalmonMatch> findSalmonsInSea(Integer matchId) {
        return this.salmonMatchRepository.findByMatchIdAndCoordinateIsNull(matchId);
    }

    public List<SalmonMatch> findSalmonsFromPlayerInSpawn(Integer playerId){
        return this.salmonMatchRepository.findAllFromPlayerInSpawn(playerId);
    }

    public List<SalmonMatch> findByMatchIdNoCoord(Integer matchId){
        return this.salmonMatchRepository.findByMatchIdAndCoordinateIsNull(matchId);
    }

    public Integer findPointsFromAllSalmonInSpawn(Integer salmonMatchId) {
        SalmonMatch sm = this.findById(salmonMatchId);
        return sm.getCoordinate().y() > 20 ? sm.getCoordinate().y() - 20 : 0;
    }

    @Transactional
    public List<SMCoordinateDTO> findByPlayerIdAsCoordinateDTO (Integer playerId) {
        return this.salmonMatchRepository.findByPlayerIdAsCoordinateDTO(playerId);
    }

    public List<SMCoordinateDTO> findByPlayerIdInRiverAsCoordinateDTO (Integer playerId) {
        return this.salmonMatchRepository.findByPlayerIdInRiverAsCoordinateDTO(playerId);
    }

    public List<SMCoordinateDTO> findByPlayerIdInSeaAsCoordinateDTO (Integer playerId) {
        return this.salmonMatchRepository.findByPlayerIdInSeaAsCoordinateDTO(playerId);
    }

    public void notifySalmonMatchSpawnUpdate (List<SalmonMatch> salmonMatches) {
        this.messagingTemplate.convertAndSend("/topic/salmons/spawn/" + salmonMatches.get(0).getMatch().getId(), "Update salmons in spawn");
    }

    public List<SalmonMatchDTO> toDTOList(List<SalmonMatch> salmonMatches) {
        return this.salmonMatchMapper.toDTOList(salmonMatches);
    }

    @Override
    @Transactional
    protected SalmonMatch updateEntityFields (SalmonMatch newSalmonMatch, SalmonMatch salmonMatchToUpdate) {
        salmonMatchToUpdate.setPlayer(newSalmonMatch.getPlayer());
        salmonMatchToUpdate.setSalmonsNumber(newSalmonMatch.getSalmonsNumber());
        salmonMatchToUpdate.setSpawningNumber(newSalmonMatch.getSpawningNumber());
        salmonMatchToUpdate.setCoordinate(newSalmonMatch.getCoordinate());
        salmonMatchToUpdate.setSalmon(newSalmonMatch.getSalmon());
        salmonMatchToUpdate.setMatch(newSalmonMatch.getMatch());
        return salmonMatchToUpdate;
    }

    public void create(Integer playerId) {
        List<SalmonMatch> salmonMatches = new ArrayList<>();
        Player player = this.playerService.findById(playerId);
        Match match = player.getMatch();
        for (int i=0; i < 4; i++) {
            SalmonMatch salmonMatch = new SalmonMatch();
            Salmon salmon = this.salmonService.findFirstByColor(player.getColor());
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
    }

    public void setSalmonImage (SalmonMatch salmonMatch) {
        if (!salmonMatch.getSalmon().getImage().contains("1") && salmonMatch.getSalmonsNumber() == 1)
            salmonMatch.setSalmon(this.salmonService.findById(salmonMatch.getSalmon().getId()+5));
    }

    private void throwExceptionsInitial(Coordinate myCoordinate, Coordinate newCoordinate, Player player, MatchTile toTravel) {
        if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("You cannot go to where yo already are");
        if (player.getEnergy() <= 0) throw new InsufficientEnergyException("Not enough energy for that move");
        if (myCoordinate == null && !newCoordinate.y().equals(0)) throw new NotValidMoveException("You can only advance from 1 to 1");
        if (player.getEnergy() < 3) {
            if (toTravel.isFull()) throw new NotValidMoveException("The tile is full and you don't have enough energy to jump!");
        }  
    }

    private void throwExceptionsGrid (Coordinate distance, Coordinate myCoordinate, Coordinate newCoordinate, MatchTile toTravel, Player player) {
        if(distance.y() < 0) throw new NotValidMoveException("You can only move forward"); 
        if(myCoordinate.y().equals(newCoordinate.y()) && myCoordinate.x().equals(1)) throw new NotValidMoveException("You can only move forward");
        if(List.of(0, 2).contains(myCoordinate.x()) && Math.abs(distance.x()) == 1 && Math.abs(distance.y()) == 1) throw new NotValidMoveException("Not allowed movement");
        if (player.getEnergy() < 3 && toTravel.isFull()) {
            throw new NotValidMoveException("The tile is full and you don't have enough energy to jump!");
        }
    }

    private MatchTile tileFullNull(SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate) {
        Coordinate newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).findFirst().orElseThrow(() -> new NotValidMoveException("No tile exists at coordinate " + newCoordinate2));
        return toTravel2;
    }

    private MatchTile handleTileFull(Coordinate newCoordinate, SalmonMatch salmonMatch, MatchTile myTile, 
        TileType myCoordinateType, MatchTile toTravel, TileType toTravelType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        Coordinate newCoordinate2 = newCoordinateToTravel(newCoordinate, x, y);
        MatchTile toTravel2 = this.matchTileService.findMyTile(matchTiles, newCoordinate2);
        if (toTravel2 == null) throw new NotValidMoveException("You cannot go to the tile or it has not still been placed!");

        int salmonLoss = 0;
        if (myTile.getTile().getType().equals(TileType.BEAR)) salmonLoss++;
        if (toTravel.getTile().getType().equals(TileType.BEAR)) salmonLoss++;
        if (toTravel2.getTile().getType().equals(TileType.BEAR)) salmonLoss++;
        if (salmonLoss > 0) salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - salmonLoss));
        return toTravel2;
    }

    private Coordinate newCoordinateToTravel(Coordinate newCoordinate, Integer x, Integer y) {
        return new Coordinate(newCoordinate.x()+x, newCoordinate.y() + y);
    }

    private void herons(List<SalmonMatch> salmonMatchesFromPlayer, List<MatchTile> matchTiles) {
        List<MatchTile> herons = matchTiles.stream()
            .filter(t -> t.getTile() != null && t.getTile().getType().equals(TileType.HERON) && t.getCoordinate() != null).toList();
        Map<Coordinate, List<SalmonMatch>> salmonsByCoord = salmonMatchesFromPlayer.stream().collect(Collectors.groupingBy(SalmonMatch::getCoordinate));
        for (MatchTile heron : herons) {
            List<SalmonMatch> salmonsHere = salmonsByCoord.get(heron.getCoordinate());
            if (salmonsHere != null && !salmonsHere.isEmpty()) {
                SalmonMatch target = salmonsHere.stream().filter(s -> s.getSalmonsNumber() == 2).findFirst().orElse(salmonsHere.get(0));
                target.setSalmonsNumber(Math.max(0, target.getSalmonsNumber() - 1));
                this.setSalmonImage(target);
                if (target.getSalmonsNumber() == 0) heron.setSalmonsNumber(heron.getSalmonsNumber() - 1);
            }
        }
    }

    private record CoordinateInteger(Coordinate coordinate, MatchTile toTravel, Integer energyUsed) {}

    private CoordinateInteger fromSea (MatchTile toTravel, MatchTile toTravel2, SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate, Integer energyUsed, Coordinate newCoordinate2, TileType toTravelType, Player player) {
        if (toTravel.isFull()) {
                toTravel2 = tileFullNull(salmonMatch, matchTiles, newCoordinate);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                    if (toTravelType.equals(TileType.BEAR)) salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                    if(toTravel2.getTile().getType().equals(TileType.BEAR)) salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                    if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                        salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                        toTravel2 = this.matchTileService.eagleToWater(toTravel2);
                    }
                }
                else throw new NotValidMoveException("The next tile is also full!");
        }
        else if (toTravelType.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel.getOrientation())) {
            energyUsed = 2;
            this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
            salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
        } 
        else if(toTravelType.equals(TileType.JUMP) && List.of(0, 1, 5).contains(toTravel.getOrientation())) {
            energyUsed = 2;
            this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
        }
        else if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
            toTravel = this.matchTileService.eagleToWater(toTravel);
        }
        return new CoordinateInteger(newCoordinate2, toTravel2, energyUsed); 
    }

    private Integer calculateMovementEnergy(TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, 
        Integer energyUsed, SalmonMatch salmonMatch, Player player, List<Integer> toTravelBearOrientations,
        List<Integer> toTravelJumpOrientations, List<Integer> myBearOrientations, List<Integer> myJumpOrientations) {

        Boolean toTravelBearHasJump = toTravelType.equals(TileType.BEAR) && toTravelBearOrientations.contains(toTravel.getOrientation());
        Boolean toTravelHasJump = toTravelType.equals(TileType.JUMP) && toTravelJumpOrientations.contains(toTravel.getOrientation());

        if (myCoordinateType.equals(TileType.BEAR)) {
            if (myBearOrientations.contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(Math.max(0, (toTravelType.equals(TileType.BEAR)) ? salmonMatch.getSalmonsNumber() - 2 : salmonMatch.getSalmonsNumber() - 1));
            } else if (toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(Math.max(0, toTravelBearHasJump ? salmonMatch.getSalmonsNumber() - 2 : toTravelHasJump ? salmonMatch.getSalmonsNumber() - 1 : salmonMatch.getSalmonsNumber()));
            }
        } 
        else if (myCoordinateType.equals(TileType.JUMP)) {
            if (myJumpOrientations.contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(Math.max(0, (toTravelType.equals(TileType.BEAR)) ? salmonMatch.getSalmonsNumber() - 1 : salmonMatch.getSalmonsNumber()));
            } else if (toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(Math.max(0, toTravelBearHasJump ? salmonMatch.getSalmonsNumber() - 1 : salmonMatch.getSalmonsNumber()));
            }
        } 
        else {
            if (toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(Math.max(0, toTravelBearHasJump ? salmonMatch.getSalmonsNumber() - 1 : salmonMatch.getSalmonsNumber()));
            }
        }
        
        return energyUsed;
    }

    private Integer above (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        return calculateMovementEnergy(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player,
                List.of(0, 1),       // toTravelBearHasJump
                List.of(0, 1, 5),    // toTravelHasJump
                List.of(3, 4),       // myCoordinateType == BEAR
                List.of(2, 3, 4)     // myCoordinateType == JUMP
        );
    }

    private Integer left (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        return calculateMovementEnergy(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player,
                List.of(0, 5),       // toTravelBearHasJump
                List.of(0, 4, 5),    // toTravelHasJump
                List.of(2, 3),       // myCoordinateType == BEAR
                List.of(1, 2, 3)     // myCoordinateType == JUMP
        );
    }

    private Integer right (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        return calculateMovementEnergy(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player,
                List.of(1, 2),       // toTravelBearHasJump
                List.of(0, 1, 2),    // toTravelHasJump
                List.of(4, 5),       // myCoordinateType == BEAR
                List.of(3, 4, 5)     // myCoordinateType == JUMP
        );
    }

    private CoordinateInteger fromGridFull (List<MatchTile> matchTiles, Coordinate myCoordinate, Coordinate newCoordinate, 
    MatchTile toTravel, MatchTile toTravel2, SalmonMatch salmonMatch, Integer energyUsed, Coordinate newCoordinate2, 
    Player player, TileType toTravelType, MatchTile myTile, TileType myCoordinateType) {
        
        MatchTile nextToTravel = toTravel2;
        Coordinate nextCoordinate = newCoordinate2;
        Integer finalEnergyUsed = energyUsed;

        if (null != myCoordinate.x()) {
            // Go up
            if (newCoordinate.x().equals(myCoordinate.x())) { 
                nextToTravel = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType, toTravel, toTravelType, matchTiles, List.of(3, 4), List.of(0, 1), 0, 1);
                if (!nextToTravel.isFull()) {
                    finalEnergyUsed = 3;
                    nextCoordinate = newCoordinateToTravel(newCoordinate, 0, 1);
                }
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            // From left
            else if (newCoordinate.x() == myCoordinate.x() + 1) {
                nextToTravel = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                if (!nextToTravel.isFull()) {
                    finalEnergyUsed = 3;
                    nextCoordinate = newCoordinateToTravel(newCoordinate, 1, 1);
                }
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            // From right
            else if (newCoordinate.x() == myCoordinate.x() - 1) {
                nextToTravel = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                if (!nextToTravel.isFull()) {
                    finalEnergyUsed = 3;
                    nextCoordinate = newCoordinateToTravel(newCoordinate, -1, 1);
                }
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            else throw new NotValidMoveException("The destination tile is full and there are none to jump to!");
            
            if (nextCoordinate != null) {
                if (player.getEnergy() < 3)
                    throw new NotValidMoveException("You need 3 energy points to jump a full tile!");
                if (nextToTravel.getTile().getType().equals(TileType.EAGLE)) {
                    if (salmonMatch.getSalmonsNumber() > 0) salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                    nextToTravel = this.matchTileService.eagleToWater(nextToTravel);
                }
                myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
                this.setSalmonImage(salmonMatch);
                return new CoordinateInteger(nextCoordinate, nextToTravel, finalEnergyUsed);
            }
        }
        throw new IllegalStateException("Move from full tile couldn't be calculated");
    }

    private Integer fromGrid (Coordinate myCoordinate,  MatchTile myTile, TileType myCoordinateType, Coordinate newCoordinate, 
    MatchTile toTravel,  TileType toTravelType, SalmonMatch salmonMatch, Integer energyUsed, Player player) {
        Coordinate distance = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
        this.throwExceptionsGrid(distance, myCoordinate, newCoordinate, toTravel, player);

        if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
            toTravel = this.matchTileService.eagleToWater(toTravel);
        }
        // Goes up
        if(newCoordinate.x().equals(myCoordinate.x()) && newCoordinate.y().equals(myCoordinate.y()+1))
            energyUsed = this.above(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player);
        // Goes left
        if(newCoordinate.x().equals(myCoordinate.x()-1))
            energyUsed = this.left(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player);
        // Goes right
        if(newCoordinate.x().equals(myCoordinate.x()+1)) {
            energyUsed = this.right(toTravelType, toTravel, myCoordinateType, myTile, energyUsed, salmonMatch, player);
        }
        myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
        return energyUsed;
    }

    public record MoveRequest(Integer salmonId, Integer x, Integer y, Boolean enterSpawn) {}

    @Transactional
    public void updateCoordinateBatch(Integer matchId, List<MoveRequest> moves) throws NotValidMoveException, InsufficientEnergyException {
        if (moves == null || moves.isEmpty()) return;

        // 1. Initiate data
        Player player = this.playerService.findMyPlayer(matchId);
        Match match = player.getMatch();
        
        if (match.getPhase() != Phase.MOVING) throw new NotValidMoveException("Now the tiles have to be placed!");
        if (!match.getActualPlayer().getId().equals(player.getId())) throw new NotValidMoveException("It is not your turn!");

        List<Player> players = this.playerService.findPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNumber();
        List<MatchTile> matchTiles = this.matchTileService.findByMatchId(match.getId());
        List<SalmonMatch> mySalmons = this.findAllFromPlayer(player.getId());

        // 2. Movement logic to each move in the batch (without saving to DB yet, just updating memory)
        for (MoveRequest move : moves) {
            Integer salmonId = move.salmonId;
            Integer targetX = move.x;
            Integer targetY = move.y;
            Boolean enterSpawn = Boolean.TRUE.equals(move.enterSpawn);
            
            SalmonMatch currentSalmon = mySalmons.stream().filter(s -> s.getId().equals(salmonId)).findFirst()
                    .orElseThrow(() -> new NotValidMoveException("Salmon not found or not yours"));

            // If salmon died in a previous step of this same turn, we ignore the rest of its movements
            if (currentSalmon.getSalmonsNumber() <= 0) continue;

            Coordinate myCoordinate = currentSalmon.getCoordinate();
            Coordinate newCoordinate = new Coordinate(targetX, targetY);

            MatchTile myTile = this.matchTileService.findMyTile(matchTiles, myCoordinate);
            MatchTile toTravel = this.matchTileService.findMyTile(matchTiles, newCoordinate);

            Integer energyUsed = 1;
            Coordinate newCoordinate2 = null;
            MatchTile toTravel2 = null;

            if (!enterSpawn) {
                this.throwExceptionsInitial(myCoordinate, newCoordinate, player, toTravel);
                TileType toTravelType = toTravel.getTile().getType();
                // --- Movement logic equal to the one in updateCoordinate ---
                if (myCoordinate == null) {
                    CoordinateInteger aux = this.fromSea(toTravel, toTravel2, currentSalmon, matchTiles, newCoordinate, energyUsed, newCoordinate2, toTravelType, player);
                    newCoordinate2 = aux.coordinate();
                    energyUsed = aux.energyUsed();
                    toTravel2 = aux.toTravel();
                } 
                else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
                    TileType myTileType = myTile.getTile().getType();
                    if (toTravel.isFull()) {
                        CoordinateInteger aux = this.fromGridFull(matchTiles, myCoordinate, newCoordinate, toTravel, toTravel2, currentSalmon, energyUsed, newCoordinate2, player, toTravelType, myTile, myTileType);
                        newCoordinate2 = aux.coordinate();
                        toTravel2 = aux.toTravel();
                        energyUsed = aux.energyUsed();
                    } 
                    else 
                        energyUsed = this.fromGrid(myCoordinate, myTile, myTileType, newCoordinate, toTravel, toTravelType, currentSalmon, energyUsed, player);
                } 
                else
                    throw new NotValidMoveException("You can only move from 1 to 1");
            }
            else {
                if (match.getRound() < 6) throw new NotValidMoveException("You cannot enter spawn until round 6!");
                energyUsed = this.enterSpawnMovement(myCoordinate, newCoordinate, myTile, currentSalmon, energyUsed, player);
                newCoordinate2 = new Coordinate(1, 21);
            }
            // --- Apply results in memory (¡Without doing this.save() here!) ---
            this.setSalmonImage(currentSalmon);
            currentSalmon.setCoordinate(newCoordinate2 != null ? newCoordinate2 : newCoordinate);
            player.setEnergy(player.getEnergy() - energyUsed);
            // Sum and remove salmons from tiles so that the next step of the loop sees the updated board
            if (currentSalmon.getSalmonsNumber() > 0 && !enterSpawn) {
                MatchTile finalTile = toTravel2 != null ? toTravel2 : toTravel;
                finalTile.setSalmonsNumber(finalTile.getSalmonsNumber() + 1);
            } 
        }

        // 3. Post movement actions (Herons)
        if (player.getEnergy() == 0) {
            List<SalmonMatch> salmonsInRiver = mySalmons.stream().filter(s -> s.getCoordinate() != null && s.getCoordinate().y() < 20).toList();
            this.herons(salmonsInRiver, matchTiles);
        }

        // 4. Batch saving to DB
        this.playerService.save(player);
        List<SalmonMatch> salmonsToDelete = mySalmons.stream().filter(s -> s.getSalmonsNumber() <= 0).toList();
        if (!salmonsToDelete.isEmpty()) this.deleteAll(salmonsToDelete);
        List<SalmonMatch> mySalmonsToSave = mySalmons.stream().filter(s -> s.getSalmonsNumber() > 0).toList();
        if (!mySalmonsToSave.isEmpty()) this.saveAll(mySalmonsToSave);
        this.matchTileService.saveAll(matchTiles);

        // 5. Global WebSocket notifications
        this.matchService.notifyPlayerAndMatchUpdate(match);
        this.matchService.notifyTilesAndSalmons(mySalmons, matchTiles);
    }

    private Integer enterSpawnMovement (Coordinate myCoordinate, Coordinate newCoordinate, MatchTile myTile, 
    SalmonMatch salmonMatch, Integer energyUsed, Player player) {
        TileType myTileType = myTile.getTile().getType();
        if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            // Go up
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
                if(myTileType.equals(TileType.BEAR) && List.of(3,4).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1)); 
                }
                else if(myTileType.equals(TileType.JUMP) && List.of(2,3,4).contains(myTile.getOrientation())) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                }
            }
            // From left
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) { 
                if(myTileType.equals(TileType.BEAR) && List.of(4,5).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                }
                else if(myTileType.equals(TileType.JUMP) && List.of(3,4,5).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                }
            }
            // From right
            else if(newCoordinate.x().equals(myCoordinate.x() - 1)){
                if(myTileType.equals(TileType.BEAR) && List.of(2,3).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(Math.max(0, salmonMatch.getSalmonsNumber() - 1));
                }
                else if(myTileType.equals(TileType.JUMP) && List.of(1,2,3).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                }
            }
            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
        }
        return energyUsed;
    }
}