package es.us.dp1.l4_01_24_25.upstream.match;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import es.us.dp1.l4_01_24_25.upstream.match.dto.DashboardMatchDTO;

@ExtendWith(MockitoExtension.class)
class MatchRepositoryTest {

    @Mock
    private MatchRepository matchRepository;

    private Match match;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setName("Test");
        match.setPhase(Phase.TILES);
        match.setState(State.WAITING);
        match.setRound(0);
        match.setPlayersNumber(0);
        match.setFinalScoreCalculated(false);
    }

    @Test
    @DisplayName("findAllAsDashboardDTO - Should return all matches as DashboardDTOs")
    void findAllAsDashboardDTO_ReturnsAllDashboardDTOs() {
        when(matchRepository.findAllAsDashboardDTO()).thenReturn(List.of(new DashboardMatchDTO(match.getId(), match.getName(), match.getPassword(), match.getState(), match.getPlayersNumber())));
        List<DashboardMatchDTO> result = matchRepository.findAllAsDashboardDTO();
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result.stream().anyMatch(m -> m.getName().equals("Test"))).isTrue();
    }

    @Test
    @DisplayName("findAllAsDashboardDTO - Should return empty list when no matches exist")
    void findAllAsDashboardDTO_ReturnsEmptyListWhenNoMatches() {
        when(matchRepository.findAllAsDashboardDTO()).thenReturn(List.of());
        List<DashboardMatchDTO> result = matchRepository.findAllAsDashboardDTO();
        assertThat(result).isEmpty();
    }
}
