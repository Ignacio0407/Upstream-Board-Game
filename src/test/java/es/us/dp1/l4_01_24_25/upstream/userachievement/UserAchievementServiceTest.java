package es.us.dp1.l4_01_24_25.upstream.userachievement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementRepository;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserRepository;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievement;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementRepository;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@ExtendWith(MockitoExtension.class)
public class UserAchievementServiceTest {
    
    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private UserAchievementService userAchievementService;

    private UserAchievement ua1;
    private UserAchievement ua2;
    private UserAchievement ua3;
    private UserAchievement ua4;
    private UserAchievement ua5;
    private User u1;
    private Achievement a1;
    private Achievement a2;
    private Achievement a3;

    @BeforeEach
    void setup() {
        ua1 = new UserAchievement();
        ua1.setId(1);

        ua2 = new UserAchievement();
        ua2.setId(2);

        u1 = new User();
        u1.setId(1);

        a1 = new Achievement();
        a1.setId(1);

        a2 = new Achievement();
        a2.setId(2);

        a3 = new Achievement();
        a3.setId(3);

        ua3 = new UserAchievement(u1, a1);
        ua4 = new UserAchievement(u1, a2);
        ua5 = new UserAchievement(u1, a3);
    }

    @Test
    void testGetAllUserAchievementsSuccess() {
        ArrayList<UserAchievement> expectedUserAchievements = (ArrayList<UserAchievement>) Arrays.asList(ua1, ua2);
        when(userAchievementRepository.findAll()).thenReturn(expectedUserAchievements);

        List<UserAchievement> result = userAchievementService.findAll();

        assertEquals(expectedUserAchievements, result);
        verify(userAchievementRepository).findAll();
    }

    @Test
    void testGetAllUserAchievementsFail() {
        List<UserAchievement> expectedUserAchievements = Arrays.asList(ua1, ua2, new UserAchievement());
        List<UserAchievement> result = userAchievementService.findAll();

        assertNotEquals(expectedUserAchievements, result);
        verify(userAchievementRepository).findAll();
    }

    @Test
    void testGetAllUserAchievementsEmpty() {
        when(userAchievementRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserAchievement> result = userAchievementService.findAll();

        assertEquals(List.of(), result);
        verify(userAchievementRepository).findAll();
    }

    @Test
    void testSaveSuccess() {
        UserAchievement userAchievement = new UserAchievement();
        when(userAchievementRepository.save(userAchievement)).thenReturn(userAchievement);

        UserAchievement result = userAchievementService.save(userAchievement);

        assertNotNull(result);
        assertEquals(userAchievement, result);
        verify(userAchievementRepository, times(1)).save(userAchievement);
    }
        
    @Test
    void testSaveFail() {
        UserAchievement userAchievement = new UserAchievement();
        DataAccessException ex = new DataAccessException("Test exception") {};
        when(userAchievementRepository.save(userAchievement)).thenThrow(ex);

        assertThrows(DataAccessException.class, () -> userAchievementService.save(userAchievement));
        verify(userAchievementRepository, times(1)).save(userAchievement);
    }

    @Test
    void testGetUserAchievementByIdSuccesfully() { 
        when(userAchievementRepository.findById(1)).thenReturn(Optional.of(ua1));

        UserAchievement result = userAchievementService.findById(1);

        assertNotNull(result);
        assertEquals(ua1, result);
        verify(userAchievementRepository).findById(1);
    }

    @Test
    void testGetUserAchievementByIdNotFound() {
        when(userAchievementRepository.findById(3)).thenReturn(Optional.empty());

        Optional<UserAchievement> result = userAchievementRepository.findById(3);

        assertEquals(Optional.empty(), result);
        verify(userAchievementRepository).findById(3);
    }

    @Test
    void testGetUserAchievementByUserIdSuccess() {
        List<UserAchievement> expected = List.of(ua3, ua4, ua5);
        when(userAchievementRepository.findByUserId(1)).thenReturn(expected);

        List<UserAchievement> result = userAchievementRepository.findByUserId(1);

        assertEquals(expected, result);
        verify(userAchievementRepository).findByUserId(1);
    }

    @Test
    void testGetUserAchievementByUserIdFail() {
        List<UserAchievement> expected = List.of(ua3, ua4, ua5);
        when(userAchievementRepository.findByUserId(1)).thenReturn(expected);

        List<UserAchievement> result = userAchievementRepository.findByUserId(1);

        assertNotEquals(List.of(ua1, ua2, ua3, ua4), result);
        verify(userAchievementRepository).findByUserId(1);
    }

    @Test
    void testGetUserAchievementByUandASuccess() {
        when(userAchievementRepository.findByUserAndAchievement(u1, a1)).thenReturn(ua1);

        UserAchievement result = userAchievementRepository.findByUserAndAchievement(u1, a1);

        assertNotNull(result);
        assertEquals(ua1, result);
        verify(userAchievementRepository).findByUserAndAchievement(u1, a1);
    }

    @Test
    void testGetUserAchievementByUandAThrowsException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("");

        when(userRepository.findById(u1.getId())).thenReturn(Optional.of(u1));
        when(achievementRepository.findById(a1.getId())).thenReturn(Optional.of(a1));
        when(userAchievementRepository.findByUserAndAchievement(u1, a1)).thenThrow(ex);

        assertThrows(ResourceNotFoundException.class, () -> userAchievementService.findByUserandAchievement(u1, a1));
        verify(achievementRepository, times(1)).findById(u1.getId());
        verify(userRepository, times(1)).findById(a1.getId());
        verify(userAchievementRepository, times(1)).findByUserAndAchievement(u1, a1);
    }

    @Test
    void testGetUserAchievementByUandAReturnsNull() {
        when(userRepository.findById(u1.getId())).thenReturn(Optional.of(u1));
        when(achievementRepository.findById(a1.getId())).thenReturn(Optional.of(a1));
        when(userAchievementRepository.findByUserAndAchievement(u1, a1)).thenReturn(null);

        UserAchievement result = userAchievementService.findByUserandAchievement(u1, a1);

        assertEquals(null, result);
        verify(achievementRepository, times(1)).findById(u1.getId());
        verify(userRepository, times(1)).findById(a1.getId());
        verify(userAchievementRepository, times(1)).findByUserAndAchievement(u1, a1);
    }

    @Test
    void testGetUserAchievementByUandAThrowsExceptionNullUser() {
        ResourceNotFoundException ex = new ResourceNotFoundException("");
        when(userAchievementRepository.findByUserAndAchievement(null, a1)).thenThrow(ex);

        assertThrows(ResourceNotFoundException.class, () -> userAchievementRepository.findByUserAndAchievement(null, a1));
        verify(userAchievementRepository, times(1)).findByUserAndAchievement(null, a1);
    }

    @Test
    void testGetUserAchievementByUandAThrowsExceptionNullAchievement() {
        ResourceNotFoundException ex = new ResourceNotFoundException("");
        when(userAchievementRepository.findByUserAndAchievement(u1, null)).thenThrow(ex);

        assertThrows(ResourceNotFoundException.class, () -> userAchievementRepository.findByUserAndAchievement(u1, null));
        verify(userAchievementRepository, times(1)).findByUserAndAchievement(u1, null);
    }

}
