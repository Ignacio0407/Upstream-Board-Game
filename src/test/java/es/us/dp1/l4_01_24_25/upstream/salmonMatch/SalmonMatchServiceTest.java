package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievement;

@ExtendWith(MockitoExtension.class)
public class SalmonMatchServiceTest {
    
    @Mock
    private SalmonMatchRepository salmonMatchRepository;

    @InjectMocks
    private SalmonMatchService salmonMatchService;

    private SalmonMatch s1;
    private SalmonMatch s2;
    private Match m1;
    private Player p1;

    @BeforeEach
    void setup() {
        s1 = new SalmonMatch();
        s1.setId(1);

        s2 = new SalmonMatch();
        s2.setId(2);

        m1 = new Match();
        m1.setId(1);

        p1 = new Player();
        p1.setId(1);

        s1.setMatch(m1);
        s2.setMatch(m1);
        s1.setPlayer(p1);
        s2.setPlayer(p1);
    }

    @Test
    void testSaveSuccess() {
        SalmonMatch s = new SalmonMatch();
        when(salmonMatchRepository.save(s)).thenReturn(s);

        SalmonMatch result = salmonMatchService.save(s);

        assertNotNull(result);
        assertEquals(s, result);
    }

    @Test
    void testSaveFail() {
        SalmonMatch s = new SalmonMatch();
        DataAccessException ex = new DataAccessException("Test exception") {};
        when(salmonMatchRepository.save(s)).thenThrow(ex);

        assertThrows(DataAccessException.class, () -> salmonMatchService.save(s));
        verify(salmonMatchRepository, times(1)).save(s);
    }

    /* @Test
    void testGetSalmonMatchByIdSuccesfully() {
        when(salmonMatchRepository.findById(1)).thenReturn(Optional.of(s1));
        
        Optional<salmonMatch> result = salmonMatchService.getPartidaSalmon(1);
        
        assertNotNull(result);
        assertEquals(s1, result.get());
        verify(salmonMatchRepository).findById(1);
    } */

    /* @Test
    void testGetSalmonMatchByIdNotFound() {
        when(salmonMatchRepository.findById(3)).thenReturn(Optional.empty());

        Optional<salmonMatch> result = salmonMatchService.getPartidaSalmon(3);

        assertEquals(Optional.empty(), result);
        verify(salmonMatchRepository).findById(3);
    } */

    @Test
    void testGetAllFromMatchSuccess() {
        List<SalmonMatch> expected = List.of(s1,s2);
        when(salmonMatchRepository.findAllFromMatch(1)).thenReturn(expected);

        List<SalmonMatch> result = salmonMatchService.getAllFromMatch(1);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(salmonMatchRepository).findAllFromMatch(1);
    }

    @Test
    void testGetAllFromMatchFail() {
        List<SalmonMatch> expected = List.of(s1,s2, new SalmonMatch());
        List<SalmonMatch> result = salmonMatchService.getAllFromMatch(1);

        assertNotNull(result);
        assertNotEquals(expected, result);
        verify(salmonMatchRepository).findAllFromMatch(1);
    }

    @Test
    void testGetAllFromMatchEmpty() {
        when(salmonMatchRepository.findAllFromMatch(1)).thenReturn(new ArrayList<>());

        List<SalmonMatch> result = salmonMatchService.getAllFromMatch(1);

        assertEquals(List.of(), result);
        verify(salmonMatchRepository).findAllFromMatch(1);
    }

    @Test
    void testGetAllFromPlayerSuccess() {
        List<SalmonMatch> expected = List.of(s1,s2);
        when(salmonMatchRepository.findAllFromPlayer(1)).thenReturn(expected);

        List<SalmonMatch> result = salmonMatchService.getAllFromPlayer(1);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(salmonMatchRepository).findAllFromPlayer(1);
    }

    @Test
    void testGetAllFromPlayerFail() {
        List<SalmonMatch> expected = List.of(s1,s2,new SalmonMatch());
        List<SalmonMatch> result = salmonMatchService.getAllFromPlayer(1);

        assertNotNull(result);
        assertNotEquals(expected, result);
        verify(salmonMatchRepository).findAllFromPlayer(1);
    }

    @Test
    void testGetAllFromPlayerEmpty() {
        when(salmonMatchRepository.findAllFromPlayer(1)).thenReturn(new ArrayList<>());

        List<SalmonMatch> result = salmonMatchService.getAllFromPlayer(1);

        assertEquals(List.of(), result);
        verify(salmonMatchRepository).findAllFromPlayer(1);
    }

    @Test
    void testDeleteSalmonMatchSuccess() {
        Integer toDelete = s1.getId();
        salmonMatchService.delete(toDelete);
        verify(salmonMatchRepository, times(1)).deleteById(toDelete);
    }

    @Test
    void testDeleteSalmonMatchThrowsException() {
        Integer toDelete = s1.getId();
        DataAccessException ex = new DataAccessException("Test Exception") {};
        doThrow(ex).when(salmonMatchRepository).deleteById(toDelete);

        assertThrows(DataAccessException.class, () -> salmonMatchService.delete(toDelete));
        verify(salmonMatchRepository, times(1)).deleteById(toDelete);
    }


}   
