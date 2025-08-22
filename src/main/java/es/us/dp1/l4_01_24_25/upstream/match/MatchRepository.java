package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.us.dp1.l4_01_24_25.upstream.match.DTO.DashboardMatchDTO;
import es.us.dp1.l4_01_24_25.upstream.match.DTO.MatchDTO;
import es.us.dp1.l4_01_24_25.upstream.model.BaseRepository;

@Repository
public interface MatchRepository extends BaseRepository<Match, MatchDTO, Integer> {

    Optional<Match> findByName(String string);

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.match.DTO.MatchDTO(m.id, m.name, m.password, m.state, m.playersNumber, m.round, m.phase, m.finalScoreCalculated, m.initialPlayer.id, m.actualPlayer.id, m.matchCreator.id) FROM Match m WHERE m.id = :id")
    Optional<MatchDTO> findByIdAsDTO(Integer id);

    @Override
    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.match.DTO.MatchDTO(m.id, m.name, m.password, m.state, m.playersNumber, m.round, m.phase, m.finalScoreCalculated, m.initialPlayer.id, m.actualPlayer.id, m.matchCreator.id) FROM Match m")
    List<MatchDTO> findAllAsDTO();

    @Query("SELECT new es.us.dp1.l4_01_24_25.upstream.match.DTO.DashboardMatchDTO(m.id, m.name, m.password, m.state, m.playersNumber) FROM Match m")
    List<DashboardMatchDTO> findAllAsDashboardDTO();

}