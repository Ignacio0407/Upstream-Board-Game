package es.us.dp1.l4_01_24_25.upstream.statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementService achievementService;

    private Achievement achievement1;
    private Achievement achievement2;

    @BeforeEach
    void setup() {
        achievement1 = new Achievement();
        achievement1.setId(1);
        achievement1.setName("First Achievement");

        achievement2 = new Achievement();
        achievement2.setId(2);
        achievement2.setName("Second Achievement");
    }

    @Nested
    @DisplayName("Get Operations Tests")
    class GetOperationsTests {

        @Test
        @DisplayName("Should get all achievements successfully")
        void testGetAchievements() {
            ArrayList<Achievement> expectedAchievements = (ArrayList<Achievement>) Arrays.asList(achievement1, achievement2);
            when(achievementRepository.findAll()).thenReturn(expectedAchievements);

            List<Achievement> result = achievementService.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(expectedAchievements, result);
            verify(achievementRepository).findAll();
        }

        @Test
        @DisplayName("Should get achievement by ID successfully")
        void testfindById_Success() {
            when(achievementRepository.findById(1)).thenReturn(Optional.of(achievement1));

            Achievement result = achievementService.findById(1);

            assertNotNull(result);
            assertEquals(achievement1.getId(), result.getId());
            assertEquals(achievement1.getName(), result.getName());
            verify(achievementRepository).findById(1);
        }

        @Test
        @DisplayName("Should return null when achievement not found by ID")
        void testfindById_NotFound() {
            when(achievementRepository.findById(99)).thenReturn(Optional.empty());

            Achievement result = achievementService.findById(99);

            assertNull(result);
            verify(achievementRepository).findById(99);
        }

        @Test
        @DisplayName("Should get achievement by name successfully")
        void testGetByName_Success() {
            when(achievementRepository.findByName("First Achievement")).thenReturn(Optional.of(achievement1));

            Achievement result = new Achievement();//achievementService.getByName("First Achievement");

            assertNotNull(result);
            assertEquals(achievement1.getName(), result.getName());
            verify(achievementRepository).findByName("First Achievement");
        }

        @Test
        @DisplayName("Should return null when achievement not found by name")
        void testGetByName_NotFound() {
            when(achievementRepository.findByName("Nonexistent")).thenReturn(null);

            Achievement result = new Achievement();//achievementService.getByName("Nonexistent");

            assertNull(result);
        }

        @Test
        @DisplayName("Should get achievements by names list successfully")
        void testGetByNames_Success() {
            //List<String> names = Arrays.asList("First Achievement", "Second Achievement");
            when(achievementRepository.findByName("First Achievement")).thenReturn(Optional.of(achievement1));
            when(achievementRepository.findByName("Second Achievement")).thenReturn(Optional.of(achievement2));

            List<Achievement> result = new ArrayList<>();//achievementService.getByNames(names);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(Arrays.asList(achievement1, achievement2), result);
            verify(achievementRepository, times(2)).findByName(any());
        }

        @Test
        @DisplayName("Should return list with achievements and null when not all achievements found by names")
        void testGetByNames_NotAllFound() {
            //List<String> names = Arrays.asList("First Achievement", "Nonexistent");

            // Simulamos el comportamiento del repositorio
            when(achievementRepository.findByName("First Achievement")).thenReturn(Optional.of(achievement1));
            when(achievementRepository.findByName("Nonexistent")).thenReturn(null);

            // Llamamos al método
            List<Achievement> result = new ArrayList<>();//achievementService.getByNames(names);

            // Verificamos el resultado
            assertEquals(2, result.size());  // Esperamos dos elementos en la lista
            assertNotNull(result.get(0));  // El primer logro no debe ser nulo
            assertNull(result.get(1));    // El segundo debe ser nulo

            // Verificamos que se haya llamado al repositorio correctamente
            verify(achievementRepository, times(2)).findByName(any());
        }

    }

    @Nested
    @DisplayName("Save Operations Tests")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save achievement successfully")
        void testSaveAchievement_Success() {
            Achievement newAchievement = new Achievement();
            newAchievement.setName("New Achievement");
            when(achievementRepository.save(any(Achievement.class))).thenReturn(newAchievement);

            Achievement result = achievementService.save(newAchievement);

            assertNotNull(result);
            assertEquals(newAchievement.getName(), result.getName());
            verify(achievementRepository).save(newAchievement);
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete achievement successfully")
        void testDeleteAchievement_Success() {
            achievementService.delete(1);
            verify(achievementRepository).deleteById(1);
        }
    }
}