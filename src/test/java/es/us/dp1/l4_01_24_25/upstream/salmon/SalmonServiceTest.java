package es.us.dp1.l4_01_24_25.upstream.salmon;

import es.us.dp1.l4_01_24_25.upstream.player.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalmonServiceTest {

    @Mock
    private SalmonRepository salmonRepository;

    @InjectMocks
    private SalmonService salmonService;

    @Test
    void testUpdateEntityFields() {
        Salmon newSalmon = new Salmon();
        newSalmon.setImage("new_salmon.png");

        Salmon salmonToUpdate = new Salmon();
        salmonToUpdate.setImage("old_salmon.png");

        Salmon result = salmonService.updateEntityFields(newSalmon, salmonToUpdate);

        assertEquals("new_salmon.png", result.getImage());
    }

    @Test
    void testFindFirstByColor() {
        Salmon salmon = new Salmon();
        salmon.setColor(Color.RED);

        when(salmonRepository.findFirstByColor(Color.RED)).thenReturn(salmon);

        Salmon result = salmonService.findFirstByColor(Color.RED);

        assertEquals(salmon, result);
        verify(salmonRepository).findFirstByColor(Color.RED);
    }
}
