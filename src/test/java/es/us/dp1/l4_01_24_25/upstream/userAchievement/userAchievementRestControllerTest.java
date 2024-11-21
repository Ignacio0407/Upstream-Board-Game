package es.us.dp1.l4_01_24_25.upstream.userAchievement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;

class UserAchievementRestControllerTest {

    @Mock
    private UserAchievementService userAchievementService;

    @InjectMocks
    private UserAchievementRestController userAchievementController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll_Positive() {
        List<UserAchievement> userAchievements = Arrays.asList(new UserAchievement(), new UserAchievement());
        when(userAchievementService.findAll()).thenReturn(userAchievements);

        ResponseEntity<List<UserAchievement>> response = userAchievementController.findAll(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userAchievements, response.getBody());
    }

    @Test
    void testFindAll_Negative() {
        when(userAchievementService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<UserAchievement>> response = userAchievementController.findAll(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testFindById_Positive() throws ResourceNotFoundException {
        UserAchievement userAchievement = new UserAchievement();
        when(userAchievementService.findById(1)).thenReturn(Optional.of(userAchievement));

        UserAchievement result = userAchievementController.findById(1);

        assertEquals(userAchievement, result);
    }

    @Test
    void testFindById_Negative() {
        when(userAchievementService.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userAchievementController.findById(1);
        });
    }

    @Test
    void testCreate_Positive() {
        UserAchievement userAchievement = new UserAchievement();
        when(userAchievementService.saveUA(userAchievement)).thenReturn(userAchievement);

        ResponseEntity<UserAchievement> response = userAchievementController.create(userAchievement);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userAchievement, response.getBody());
    }
}