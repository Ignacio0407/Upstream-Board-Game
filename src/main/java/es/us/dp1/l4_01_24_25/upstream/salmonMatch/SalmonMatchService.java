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
import es.us.dp1.l4_01_24_25.upstream.exceptions.NoCapacityException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.NotValidMoveException;
import es.us.dp1.l4_01_24_25.upstream.exceptions.OnlyMovingForwardException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTile;
import es.us.dp1.l4_01_24_25.upstream.matchTile.MatchTileService;
import es.us.dp1.l4_01_24_25.upstream.matchTile.DTO.MatchTileDTO;
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

    public void notifySalmonMatchesUpdateToDTO (List<SalmonMatch> salmonMatches) {
        this.messagingTemplate.convertAndSend("/topic/herons/" + salmonMatches.get(0).getMatch().getId(), this.salmonMatchMapper.toDTOList(salmonMatches));
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

    public void setSalmonImage (SalmonMatch salmonMatch) {
        if (!salmonMatch.getSalmon().getImage().contains("1") && salmonMatch.getSalmonsNumber() == 1)
            salmonMatch.setSalmon(this.salmonService.findById(salmonMatch.getSalmon().getId()+5));
    }

    private void throwExceptionsInitial(Coordinate myCoordinate, Coordinate newCoordinate, Player player, MatchTile toTravel) {
        if(myCoordinate != null && myCoordinate.equals(newCoordinate)) throw new NotValidMoveException("You cannot go to where yo already are");
        if (player.getEnergy() <= 0) throw new InsufficientEnergyException("Not enough energy for that move");
        if (myCoordinate == null && newCoordinate.y() != 0) throw new NotValidMoveException("You can only advance from 1 to 1");
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
        MatchTile toTravel2 = matchTiles.stream().filter(mT -> mT.getCoordinate() != null && mT.getCoordinate().equals(newCoordinate2)).findFirst().orElseThrow(() -> new NotValidMoveException("No tile exists at coordinate " + newCoordinate2));;
        TileType toTravelType2 = toTravel2.getTile().getType();
        if(toTravelType2.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel2.getOrientation())) 
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber() - 1);
        return toTravel2;
    }

    private MatchTile handleTileFull(Coordinate newCoordinate, SalmonMatch salmonMatch, MatchTile myTile, 
        TileType myCoordinateType, MatchTile toTravel, TileType toTravelType, List<MatchTile> matchTiles, List<Integer> from, List<Integer> to, Integer x, Integer y) {
        Coordinate newCoordinate2 = newCoordinateToTravel(newCoordinate, x, y);
        MatchTile toTravel2 = this.matchTileService.findMyTile(matchTiles, newCoordinate2);
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

    private void herons(Player player, Match match, Integer numPlayers) {
        List<SalmonMatch> salmonMatchesFromPlayer = this.findAllFromPlayerInRiver(player.getId());
        List<MatchTile> herons = this.matchTileService.findHeronWithCoordsFromGame(match.getId());
        Map<Coordinate, List<SalmonMatch>> salmonsByCoord = salmonMatchesFromPlayer.stream().collect(Collectors.groupingBy(SalmonMatch::getCoordinate));
        List<SalmonMatch> salmonMatchesUpdated = new ArrayList<>();
        List<MatchTile> heronsUpdated = new ArrayList<>();
        List<SalmonMatch> salmonMatchesToDelete = new ArrayList<>();
        List<Integer> deletedSalmonsIds = new ArrayList<>();
        for(MatchTile heron : herons) {
            List<SalmonMatch> salmonsHere = salmonsByCoord.get(heron.getCoordinate());
            if (salmonsHere != null && !salmonsHere.isEmpty()) {
                SalmonMatch target = salmonsHere.stream().filter(s -> s.getSalmonsNumber() == 2).findFirst().orElse(salmonsHere.get(0));
                target.setSalmonsNumber(target.getSalmonsNumber()-1);
                this.setSalmonImage(target);
                if(target.getSalmonsNumber()==0) {
                    salmonMatchesToDelete.add(target);
                    heron.setSalmonsNumber(heron.getSalmonsNumber()-1);
                    heronsUpdated.add(this.matchTileService.save(heron));
                    deletedSalmonsIds.add(target.getId());
                }
                else salmonMatchesUpdated.add(target);
            }
        }
        if (!salmonMatchesToDelete.isEmpty()) {
            this.deleteAll(salmonMatchesToDelete);
            if (!heronsUpdated.isEmpty() && salmonMatchesUpdated.isEmpty()) this.matchService.notifyHeronsUpdated(salmonMatchesUpdated, heronsUpdated, deletedSalmonsIds, match.getId());
        }
        else if (!salmonMatchesUpdated.isEmpty()) {
            salmonMatchesUpdated = this.saveAll(salmonMatchesUpdated);
            if (!heronsUpdated.isEmpty() && !deletedSalmonsIds.isEmpty()) this.matchService.notifyHeronsUpdated(salmonMatchesUpdated, heronsUpdated, deletedSalmonsIds, match.getId());
            else this.notifySalmonMatchesUpdateToDTO(salmonMatchesUpdated);
        }
    }

    private SalmonMatchDTO processSalmonMovement(SalmonMatch salmonMatch, MatchTile myTile, MatchTile toTravel, Player player, Match match, 
            Coordinate newCoordinate, Integer energyUsed, List<Player> players, Integer numPlayers) {
        salmonMatch.setCoordinate(newCoordinate);
        player.setEnergy(player.getEnergy() - energyUsed);
        this.playerService.save(player);
        if (salmonMatch.getSalmonsNumber() > 0) {
            if (!toTravel.isFull()) toTravel.setSalmonsNumber(toTravel.getSalmonsNumber() + 1);
            this.matchTileService.save(toTravel);
            this.save(salmonMatch);
        }
        else this.delete(salmonMatch);
        if (player.getEnergy() == 0) {
            herons(player, match, numPlayers);
        }
        SalmonMatchDTO salmon = this.salmonMatchMapper.toDTO(salmonMatch);
        MatchTileDTO tileToTravel = this.matchTileService.toDTO(toTravel);
        MatchTileDTO myTileDTO = this.matchTileService.toDTO(myTile);
        if (myTile != null) {
            this.matchTileService.save(myTile);
            this.messagingTemplate.convertAndSend("/topic/movingSalmon/" + match.getId(), Map.of("salmon", salmon, "myTile", myTileDTO, "toTravel", tileToTravel));
        }
        else this.messagingTemplate.convertAndSend("/topic/movingSalmonFromSea/" + match.getId(), Map.of("salmon", salmon, "toTravel", tileToTravel));
        this.matchService.notifyPlayerAndMatchUpdate(match);
        return salmon;
    }

    private Boolean bearBoolean(MatchTile myTile, MatchTile toTravel, TileType myCoordinateType, TileType toTravelType, List<Integer> from, List<Integer> to) {
        return myCoordinateType.equals(TileType.BEAR) && from.contains(myTile.getOrientation()) ||
            toTravelType.equals(TileType.BEAR) && to.contains(toTravel.getOrientation());
    }

    private record CoordinateInteger(Coordinate coordinate, MatchTile toTravel, Integer energyUsed) {}

    private CoordinateInteger fromSea (MatchTile toTravel, MatchTile toTravel2, SalmonMatch salmonMatch, List<MatchTile> matchTiles, Coordinate newCoordinate, Integer energyUsed, Coordinate newCoordinate2, TileType toTravelType, Player player) {
        if (toTravel.isFull()) {
                toTravel2 = tileFullNull(salmonMatch, matchTiles, newCoordinate);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    newCoordinate2 = new Coordinate(newCoordinate.x(), newCoordinate.y()+1);
                    if (toTravelType.equals(TileType.BEAR)) salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                        salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                        toTravel2 = this.matchTileService.eagleToWater(toTravel2);
                    }
                }
                else throw new NotValidMoveException("The next tile is also full!");
        }
        else if (toTravelType.equals(TileType.BEAR) && List.of(0, 1).contains(toTravel.getOrientation())) {
            energyUsed = 2;
            this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
        } 
        else if(toTravelType.equals(TileType.JUMP) && List.of(0, 1, 5).contains(toTravel.getOrientation())) {
            energyUsed = 2;
            this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
        }
        else if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
            toTravel = this.matchTileService.eagleToWater(toTravel);
        }
        return new CoordinateInteger(newCoordinate2, toTravel2, energyUsed); 
    }

    private Integer above (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        Boolean toTravelBearHasJump = toTravelType.equals(TileType.BEAR) && List.of(0,1).contains(toTravel.getOrientation());
        Boolean toTravelHasJump = toTravelType.equals(TileType.JUMP) && List.of(0,1,5).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(3,4).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if (toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-2:toTravelHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(2,3,4).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }
        }
        else {
            if(toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        return energyUsed;
    }

    private Integer left (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        Boolean toTravelBearHasJump = toTravelType.equals(TileType.BEAR) && List.of(0,5).contains(toTravel.getOrientation());
        Boolean toTravelHasJump = toTravelType.equals(TileType.JUMP) && List.of(0,4,5).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(2,3).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if(toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-2:toTravelHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
                }
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(1,2,3).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }
        }
        else {
            if(toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        return energyUsed;
    }

    private Integer right (TileType toTravelType, MatchTile toTravel, TileType myCoordinateType, MatchTile myTile, Integer energyUsed, SalmonMatch salmonMatch, Player player) {
        Boolean toTravelBearHasJump = toTravelType.equals(TileType.BEAR) && List.of(1,2).contains(toTravel.getOrientation());
        Boolean toTravelHasJump = toTravelType.equals(TileType.JUMP) && List.of(0,1,2).contains(toTravel.getOrientation());
        if(myCoordinateType.equals(TileType.BEAR)) {
            if(List.of(4,5).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-2:salmonMatch.getSalmonsNumber()-1);
            }
            else {
                if(toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-2:toTravelHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber()); 
                }
            }
        }
        else if(myCoordinateType.equals(TileType.JUMP)) {
            if(List.of(3,4,5).contains(myTile.getOrientation())) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber((toTravelType.equals(TileType.BEAR))?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
            else {
                if(toTravelBearHasJump || toTravelHasJump) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                    salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
                }
            }
        }
        else {
            if(toTravelBearHasJump || toTravelHasJump) {
                energyUsed = 2;
                this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                salmonMatch.setSalmonsNumber(toTravelBearHasJump?salmonMatch.getSalmonsNumber()-1:salmonMatch.getSalmonsNumber());
            }
        }
        return energyUsed;
    }

    private CoordinateInteger fromGridFull (List<MatchTile> matchTiles, Coordinate myCoordinate, Coordinate newCoordinate, 
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
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            // Si voy al centro desde la izquierda
            else if (newCoordinate.x() == myCoordinate.x() + 1) {
                toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(4, 5), List.of(1, 2), 1, 1);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = newCoordinateToTravel(newCoordinate, 1, 1);
                }
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            // Si voy al centro desde la derecha
            else if (newCoordinate.x() == myCoordinate.x() - 1) {
                toTravel2 = handleTileFull(newCoordinate, salmonMatch, myTile, myCoordinateType,  toTravel, toTravelType, matchTiles, List.of(2, 3), List.of(0, 5), -1, 1);
                if (!toTravel2.isFull()) {
                    energyUsed = 3;
                    newCoordinate2 = newCoordinateToTravel(newCoordinate, -1, 1);
                }
                else throw new NotValidMoveException("The destination tile and the next are full!");
            }
            else throw new NotValidMoveException("The destination tile is full and there are none to jump to!");
            if (newCoordinate2 != null) {
                if (player.getEnergy() < 3)
                    throw new NotValidMoveException("You need 3 energy points to jump a full tile!");
                if (toTravel2.getTile().getType().equals(TileType.EAGLE)) {
                    if (salmonMatch.getSalmonsNumber() > 0) salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                    toTravel2 = this.matchTileService.eagleToWater(toTravel2);
                }
                myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
                this.setSalmonImage(salmonMatch);
                return new CoordinateInteger(newCoordinate2, toTravel2, energyUsed);
            }
        }
        throw new IllegalStateException("Move from full tile couldn't be calculated");
    }

    private Integer fromGrid (Coordinate myCoordinate,  MatchTile myTile, TileType myCoordinateType, Coordinate newCoordinate, 
    MatchTile toTravel,  TileType toTravelType, SalmonMatch salmonMatch, Integer energyUsed, Player player) {
        Coordinate distance = new Coordinate((newCoordinate.x() - myCoordinate.x()), (newCoordinate.y() - myCoordinate.y()));
        this.throwExceptionsGrid(distance, myCoordinate, newCoordinate, toTravel, player);

        if(toTravelType.equals(TileType.EAGLE)) {
            salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
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

    @Transactional
    public void updateCoordinate(Integer id, Map<String,Integer> coordinate) throws NotValidMoveException,  InsufficientEnergyException, OnlyMovingForwardException, NoCapacityException {
        SalmonMatch salmonMatch = this.findById(id);
        Match match = salmonMatch.getMatch();
        if (match.getPhase() != Phase.MOVING)
            throw new NotValidMoveException("Now the tiles have to be placed!");
        Player player = salmonMatch.getPlayer();
        List<Player> players = this.playerService.findPlayersByMatch(match.getId());
        Integer numPlayers = match.getPlayersNumber();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(coordinate.get("x"), coordinate.get("y"));
        List<MatchTile> matchTiles = this.matchTileService.findByMatchId(match.getId());
        MatchTile myTile = null;
        MatchTile toTravel = this.matchTileService.findMyTile(matchTiles, newCoordinate);
        TileType toTravelType = toTravel.getTile().getType();
        Coordinate newCoordinate2 = null;
        MatchTile toTravel2 = null;
        Integer energyUsed = 1;
        this.throwExceptionsInitial(myCoordinate, newCoordinate, player, toTravel);

        if (myCoordinate == null) {
            CoordinateInteger aux = this.fromSea(toTravel, toTravel2, salmonMatch, matchTiles, newCoordinate, energyUsed, newCoordinate2, toTravelType, player);
            newCoordinate2 = aux.coordinate();
            energyUsed = aux.energyUsed();
            toTravel2 = aux.toTravel();
        }
        // I'm in grid
        else if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            myTile = this.matchTileService.findMyTile(matchTiles, myCoordinate);
            TileType myCoordinateType = myTile.getTile().getType();;
            if (toTravel.isFull()) {
                CoordinateInteger aux = this.fromGridFull(matchTiles, myCoordinate, newCoordinate, toTravel, toTravel2, salmonMatch, energyUsed, newCoordinate2, player, match, players, numPlayers, toTravelType, myTile, myCoordinateType);
                newCoordinate2 = aux.coordinate();
                toTravel2 = aux.toTravel();
                energyUsed = aux.energyUsed();
            }
            else
                energyUsed = this.fromGrid(myCoordinate, myTile, myCoordinateType, newCoordinate, toTravel, toTravelType, salmonMatch, energyUsed, player);
        }
        else throw new NotValidMoveException("You can only move from 1 to 1");
        setSalmonImage(salmonMatch);
        if (newCoordinate2 == null )  this.processSalmonMovement(salmonMatch, myTile, toTravel, player, match, newCoordinate, energyUsed, players, numPlayers);
        else this.processSalmonMovement(salmonMatch, myTile, toTravel2, player, match, newCoordinate2, energyUsed, players, numPlayers);
    }

    public List<SalmonMatchDTO> create(Integer playerId) {
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
        return this.salmonMatchMapper.toDTOList(salmonMatches);
    }

    private void enterSpawnMovement (Coordinate myCoordinate, Coordinate newCoordinate, TileType tileType, MatchTile myTile, 
    SalmonMatch salmonMatch, Coordinate updateCoordinate, Integer energyUsed, Player player) {
        if (Math.abs(myCoordinate.x() - newCoordinate.x()) <= 1 && Math.abs(myCoordinate.y() - newCoordinate.y()) <= 1) {
            // Go up
            if(newCoordinate.y() == myCoordinate.y() + 1 && myCoordinate.x().equals(newCoordinate.x())) { 
                if(tileType.equals(TileType.BEAR) && List.of(3,4).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1); 
                }
                else if(tileType.equals(TileType.JUMP) && List.of(2,3,4).contains(myTile.getOrientation())) {
                    energyUsed = 2;
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);
                }
            }
            // From left
            else if(newCoordinate.x().equals(myCoordinate.x() + 1)) { 
                if(tileType.equals(TileType.BEAR) && List.of(4,5).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                }
                else if(tileType.equals(TileType.JUMP) && List.of(3,4,5).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                }
            }
            // From right
            else if(newCoordinate.x().equals(myCoordinate.x() - 1)){
                if(tileType.equals(TileType.BEAR) && List.of(2,3).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                    salmonMatch.setSalmonsNumber(salmonMatch.getSalmonsNumber()-1);
                }
                else if(tileType.equals(TileType.JUMP) && List.of(1,2,3).contains(myTile.getOrientation())) {
                    energyUsed = 2; 
                    this.playerService.throwExceptionNotEnoughEnergy(player, energyUsed);  
                }
            }

            myTile.setSalmonsNumber(myTile.getSalmonsNumber()-1);
            this.matchTileService.save(myTile);
            salmonMatch.setCoordinate(updateCoordinate);
            player.setEnergy(player.getEnergy() - energyUsed);
            if (salmonMatch.getSalmonsNumber() > 0) this.save(salmonMatch);
            else this.delete(salmonMatch);
        }
    }

    public void enterSpawn(Integer id) {
        SalmonMatch salmonMatch = this.findById(id);
        Match match = salmonMatch.getMatch();
        if (match.getRound() < 6) throw new NotValidMoveException("You can only enter the spawn from round 6!");
        Player player = salmonMatch.getPlayer();
        Coordinate myCoordinate = salmonMatch.getCoordinate();
        Coordinate newCoordinate = new Coordinate(1,5);
        Coordinate updateCoordinate = new Coordinate(1, 21);
        Integer energyUsed = 1;
        MatchTile myTile = this.matchTileService.findMyTile(this.matchTileService.findByMatchId(match.getId()), myCoordinate);
        TileType tileType = myTile.getTile().getType();

        this.enterSpawnMovement(myCoordinate, newCoordinate, tileType, myTile, salmonMatch, updateCoordinate, energyUsed, player);

        SalmonMatchDTO updated = this.salmonMatchMapper.toDTO(salmonMatch);
        this.messagingTemplate.convertAndSend("/topic/salmon/spawn/enter/" + match.getId(), Map.of("salmonMatch", updated, "matchTile", this.matchTileService.toDTO(myTile)));
        this.playerService.save(player);
        this.matchService.notifyPlayerAndMatchUpdate(match);
    }

}