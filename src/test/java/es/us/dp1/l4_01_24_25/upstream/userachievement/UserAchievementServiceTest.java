package es.us.dp1.l4_01_24_25.upstream.userachievement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.statistic.Achievement;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementService;
import es.us.dp1.l4_01_24_25.upstream.statistic.AchievementUnlocker;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievement;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementRepository;
import es.us.dp1.l4_01_24_25.upstream.userAchievement.UserAchievementService;

@ExtendWith(MockitoExtension.class)
public class UserAchievementServiceTest {

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private UserService userService;

    @Mock
    private AchievementService achievementService;

    @Mock
    private AchievementUnlocker achievementUnlocker;

    @InjectMocks
    private UserAchievementService userAchievementService;

    @Captor
    private ArgumentCaptor<List<UserAchievement>> userAchievementListCaptor;

    @Test
    @DisplayName("checkAndUnlockAchievements - Should unlock multiple achievements when criteria met and not already possessed")
    void checkAndUnlockAchievements_UnlockNew_SavesAchievements() {
        // Arrange
        Integer userId = 1;
        User user = new User();
        user.setId(userId);

        Achievement a1 = new Achievement(); a1.setId(101); // Already has
        Achievement a2 = new Achievement(); a2.setId(102); // Will unlock
        Achievement a3 = new Achievement(); a3.setId(103); // Won't unlock (criteria not met)

        when(userService.findById(userId)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a1, a2, a3));

        UserAchievement ua1 = new UserAchievement(user, a1);
        when(userAchievementRepository.findByUserId(userId)).thenReturn(new ArrayList<>(List.of(ua1)));

        when(achievementUnlocker.checkUnlock(user, a2)).thenReturn(true);
        when(achievementUnlocker.checkUnlock(user, a3)).thenReturn(false);

        // Act
        userAchievementService.checkAndUnlockAchievements(userId);

        // Assert
        verify(userAchievementRepository).saveAll(userAchievementListCaptor.capture());
        List<UserAchievement> saved = userAchievementListCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getAchievement().getId()).isEqualTo(102);
    }

    @Test
    @DisplayName("checkAndUnlockAchievements - Should not save anything if no new achievements meet criteria")  
    void checkAndUnlockAchievements_NothingToUnlock_SavesEmptyList() {
        // Arrange
        Integer userId = 1;
        User user = new User();
        user.setId(userId);

        Achievement a1 = new Achievement(); a1.setId(101);

        when(userService.findById(userId)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a1));
        when(userAchievementRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(achievementUnlocker.checkUnlock(user, a1)).thenReturn(false);

        // Act
        userAchievementService.checkAndUnlockAchievements(userId);

        // Assert
        verify(userAchievementRepository).saveAll(Collections.emptyList());
    }

    @Test
    @DisplayName("unlockRules - Should unlock hardcoded achievement 4 for valid user")
    void unlockRules_ValidUser_SavesNewAchievement() throws Exception {
        // Arrange
        String username = "testUser";
        User user = new User();
        user.setId(1);
        user.setName(username);

        Achievement achievement4 = new Achievement();
        achievement4.setId(4);

        when(userService.findUserByName(username)).thenReturn(user);
        when(achievementService.findById(4)).thenReturn(achievement4);
        when(userAchievementRepository.findByUserIdAndAchievementId(1, 4)).thenReturn(null);
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArguments()[0]);  

        // Act
        UserAchievement result = userAchievementService.unlockRules(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getAchievement()).isEqualTo(achievement4);
        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    @DisplayName("unlockRules - Should throw generic Exception if achievement already unlocked")
    void unlockRules_AlreadyUnlocked_ThrowsException() {
        // Arrange
        String username = "testUser";
        User user = new User();
        user.setId(1);
        user.setName(username);

        Achievement achievement4 = new Achievement();
        achievement4.setId(4);

        when(userService.findUserByName(username)).thenReturn(user);
        when(achievementService.findById(4)).thenReturn(achievement4);
        when(userAchievementRepository.findByUserIdAndAchievementId(1, 4)).thenReturn(new UserAchievement());   

        // Act & Assert
        assertThatThrownBy(() -> userAchievementService.unlockRules(username))
                .isExactlyInstanceOf(Exception.class);

        verify(userAchievementRepository, never()).save(any());
    }
}
