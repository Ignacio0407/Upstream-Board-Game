package upstream.match;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import upstream.match.dto.DashboardMatchDTO;
import upstream.match.dto.MatchDTO;
import upstream.model.BaseRepository;

@Repository
public interface MatchRepository extends BaseRepository<Match, MatchDTO, Integer> {

    Optional<Match> findByName(String string);

    @Override
    @Query("SELECT new upstream.match.dto.MatchDTO(m.id, m.name, m.password, m.state, m.playersNumber, m.round, m.phase, m.finalScoreCalculated, m.initialPlayer.id, m.actualPlayer.id, m.matchCreator.id) FROM Match m WHERE m.id = :id")
    Optional<MatchDTO> findByIdAsDTO(Integer id);

    @Override
    @Query("SELECT new upstream.match.dto.MatchDTO(m.id, m.name, m.password, m.state, m.playersNumber, m.round, m.phase, m.finalScoreCalculated, m.initialPlayer.id, m.actualPlayer.id, m.matchCreator.id) FROM Match m")
    List<MatchDTO> findAllAsDTO();

    @Query("SELECT new upstream.match.dto.DashboardMatchDTO(m.id, m.name, m.password, m.state, m.playersNumber) FROM Match m")
    List<DashboardMatchDTO> findAllAsDashboardDTO();

}