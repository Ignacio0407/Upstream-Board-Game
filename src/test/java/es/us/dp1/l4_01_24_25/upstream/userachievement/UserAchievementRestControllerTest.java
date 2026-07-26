package es.us.dp1.l4_01_24_25.upstream.userachievement;

import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievement;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementRestController;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAchievementRestControllerTest {

    @Mock
    private UserAchievementService userAchievementService;

    @InjectMocks
    private UserAchievementRestController controller;

    @Test
    void testUnlockRules() throws Exception {
        UserAchievement ua = new UserAchievement();
        ua.setId(1);

        when(userAchievementService.unlockRules("john_doe")).thenReturn(ua);

        ResponseEntity<UserAchievement> response = controller.unlockRules("john_doe");

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ua, response.getBody());
        verify(userAchievementService).unlockRules("john_doe");
    }
}
