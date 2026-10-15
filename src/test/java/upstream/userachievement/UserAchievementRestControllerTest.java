package upstream.userachievement;

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
    void testUnlockRules() {
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
