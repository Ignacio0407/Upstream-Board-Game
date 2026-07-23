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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
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

    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private UserService userService;
    @Mock private AchievementService achievementService;
    @Mock private AchievementUnlocker achievementUnlocker;

    @InjectMocks
    private UserAchievementService userAchievementService;

    @Captor
    private ArgumentCaptor<List<UserAchievement>> listCaptor;

    // ── findByUserId ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnUserAchievementsWhenUserExists() {
        User user = new User(); user.setId(1);
        Achievement a = new Achievement(); a.setId(101);
        UserAchievement ua = new UserAchievement(user, a);

        when(userAchievementRepository.findByUserId(1)).thenReturn(List.of(ua));

        List<UserAchievement> result = userAchievementService.findByUserId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAchievement().getId()).isEqualTo(101);
    }

    @Test
    void shouldThrowWhenFindByUserIdWithNull() {
        assertThatThrownBy(() -> userAchievementService.findByUserId(null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── findByUserAndAchievement ──────────────────────────────────────────────

    @Test
    void shouldThrowWhenUserIsNull() {
        Achievement a = new Achievement();
        assertThatThrownBy(() -> userAchievementService.findByUserandAchievement(null, a))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenAchievementIsNull() {
        User user = new User();
        assertThatThrownBy(() -> userAchievementService.findByUserandAchievement(user, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── checkAndUnlockAchievements ────────────────────────────────────────────

    @Test
    void shouldUnlockOnlyNewAchievementsWhenCriteriaMet() {
        User user = new User(); user.setId(1);
        Achievement a1 = new Achievement(); a1.setId(101);
        Achievement a2 = new Achievement(); a2.setId(102);
        Achievement a3 = new Achievement(); a3.setId(103);

        when(userService.findById(1)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a1, a2, a3));
        when(userAchievementRepository.findByUserId(1))
                .thenReturn(new ArrayList<>(List.of(new UserAchievement(user, a1))));
        when(achievementUnlocker.checkUnlock(user, a2)).thenReturn(true);
        when(achievementUnlocker.checkUnlock(user, a3)).thenReturn(false);

        userAchievementService.checkAndUnlockAchievements(1);

        verify(userAchievementRepository).saveAll(listCaptor.capture());
        List<UserAchievement> saved = listCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getAchievement().getId()).isEqualTo(102);
    }

    @Test
    void shouldSaveEmptyListWhenNoCriteriaMet() {
        User user = new User(); user.setId(1);
        Achievement a1 = new Achievement(); a1.setId(101);

        when(userService.findById(1)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a1));
        when(userAchievementRepository.findByUserId(1)).thenReturn(Collections.emptyList());
        when(achievementUnlocker.checkUnlock(user, a1)).thenReturn(false);

        userAchievementService.checkAndUnlockAchievements(1);

        verify(userAchievementRepository).saveAll(listCaptor.capture());
        assertThat(listCaptor.getValue()).isEmpty();
    }

    @Test
    void shouldNotCheckAlreadyOwnedAchievements() {
        User user = new User(); user.setId(1);
        Achievement a1 = new Achievement(); a1.setId(101);

        when(userService.findById(1)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a1));
        when(userAchievementRepository.findByUserId(1))
                .thenReturn(List.of(new UserAchievement(user, a1)));

        userAchievementService.checkAndUnlockAchievements(1);

        verify(achievementUnlocker, never()).checkUnlock(any(), any());
    }

    // ── unlockRules ───────────────────────────────────────────────────────────

    @Test
    void shouldUnlockRulesAchievementWhenNotYetUnlocked() throws Exception {
        User user = new User(); user.setId(1); user.setName("testUser");
        Achievement a4 = new Achievement(); a4.setId(4);

        when(userService.findUserByName("testUser")).thenReturn(user);
        when(achievementService.findById(4)).thenReturn(a4);
        when(userAchievementRepository.findByUserIdAndAchievementId(1, 4)).thenReturn(null);
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArguments()[0]);

        UserAchievement result = userAchievementService.unlockRules("testUser");

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getAchievement()).isEqualTo(a4);
        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void shouldThrowWhenRulesAchievementAlreadyUnlocked() {
        User user = new User(); user.setId(1); user.setName("testUser");
        Achievement a4 = new Achievement(); a4.setId(4);

        when(userService.findUserByName("testUser")).thenReturn(user);
        when(achievementService.findById(4)).thenReturn(a4);
        when(userAchievementRepository.findByUserIdAndAchievementId(1, 4))
                .thenReturn(new UserAchievement());

        assertThatThrownBy(() -> userAchievementService.unlockRules("testUser"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        verify(userAchievementRepository, never()).save(any());
    }

    // ── findUserAchievements ──────────────────────────────────────────────────

    @Test
    void shouldReturnAchievementsAfterCheckingUnlocks() {
        User user = new User(); user.setId(1);
        Achievement a = new Achievement(); a.setId(101);
        UserAchievement ua = new UserAchievement(user, a);

        when(userService.findById(1)).thenReturn(user);
        when(achievementService.findAll()).thenReturn(List.of(a));
        when(userAchievementRepository.findByUserId(1)).thenReturn(List.of(ua));

        List<Achievement> result = userAchievementService.findUserAchievements(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(101);
    }
}
