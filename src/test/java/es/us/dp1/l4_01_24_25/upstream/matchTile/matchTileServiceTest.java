package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.dao.DataAccessException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class MatchTileServiceTest {

    @Mock
    private MatchTileRepository matchTileRepository;

    @Mock
    private MatchTileMapper matchTileMapper;

    @Mock
    private TileService tileService;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchTileService matchTileService;

    @ParameterizedTest
    @CsvSource({
        "1, 2",
        "3, 0"
    })
    void findByMatchId_ShouldReturnList(Integer matchId, int expectedSize) {
        // Setup
        List<MatchTile> tiles = Collections.nCopies(expectedSize, new MatchTile());
        when(matchTileRepository.findByMatchId(matchId)).thenReturn(tiles);

        // Test
        List<MatchTile> result = matchTileService.findByMatchId(matchId);

        // Verify
        assertEquals(expectedSize, result.size());
    }

    @Test
    void updateCoordinate_ShouldUpdateWhenValid() {
        // Setup
        MatchTile tile = new MatchTile();
        tile.setId(1);
        Match match = new Match();
        match.setRound(1);
        tile.setMatch(match);
        
        when(matchTileRepository.findById(1)).thenReturn(Optional.of(tile));
        when(matchTileRepository.findByMatchId(any())).thenReturn(Collections.emptyList());
        when(matchTileRepository.save(any())).thenReturn(tile);
        when(matchTileMapper.toDTO(any())).thenReturn(new MatchTileDTO());

        // Test
        Map<String, Integer> updates = new HashMap<>();
        updates.put("x", 1);
        updates.put("y", 1);
        MatchTileDTO result = matchTileService.updateCoordinate(1, updates);

        // Verify
        assertNotNull(result);
        verify(matchTileRepository).save(tile);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 4, true",
        "0, 3, false",
        "1, 5, true"
    })
    void validateTilePlacement_ShouldValidateCorrectly(int round, int y, boolean shouldThrow) {
        // Test
        if (shouldThrow) {
            assertThrows(ResourceNotFoundException.class, () -> 
                matchTileService.validateTilePlacement(round, y));
        } else {
            assertDoesNotThrow(() -> matchTileService.validateTilePlacement(round, y));
        }
    }

    @Test
    void createMultipleMatchTiles_ShouldCreateAllTiles() throws DataAccessException {
        // Setup
        when(matchService.findById(any())).thenReturn(new Match());
        when(tileService.findById(any())).thenReturn(new Tile());
        when(matchTileMapper.toDTOList(any())).thenReturn(Collections.emptyList());

        // Test
        List<MatchTileDTO> result = matchTileService.createMultipleMatchTiles(1);

        // Verify
        verify(matchTileRepository, atLeastOnce()).save(any());
        assertNotNull(result);
    }

    @Test
    void findAllAsDTO_ShouldReturnListOfDTOs() {
        // Arrange
        MatchTile tile1 = new MatchTile();
        MatchTile tile2 = new MatchTile();
        MatchTileDTO dto1 = new MatchTileDTO();
        MatchTileDTO dto2 = new MatchTileDTO();
        
        when(matchTileRepository.findAll()).thenReturn(Arrays.asList(tile1, tile2));
        when(matchTileMapper.toDTO(tile1)).thenReturn(dto1);
        when(matchTileMapper.toDTO(tile2)).thenReturn(dto2);

        // Act
        List<MatchTileDTO> result = matchTileService.findAllAsDTO();

        // Assert
        assertEquals(2, result.size());
        verify(matchTileRepository).findAll();
        verify(matchTileMapper).toDTO(tile1);
        verify(matchTileMapper).toDTO(tile2);
    }

    // Test parametrizado para findByIdAsDTO
    @ParameterizedTest
    @CsvSource({"1", "2", "5"})
    void findByIdAsDTO_ShouldReturnDTO(Integer id) {
        // Arrange
        MatchTile tile = new MatchTile();
        MatchTileDTO dto = new MatchTileDTO();
        
        when(matchTileRepository.findById(id)).thenReturn(Optional.of(tile));
        when(matchTileMapper.toDTO(tile)).thenReturn(dto);

        // Act
        MatchTileDTO result = matchTileService.findByIdAsDTO(id);

        // Assert
        assertNotNull(result);
        verify(matchTileRepository).findById(id);
        verify(matchTileMapper).toDTO(tile);
    }

    // Test para saveAsDTO
    @Test
    void saveAsDTO_ShouldSaveAndReturnDTO() {
        // Arrange
        MatchTileDTO dto = new MatchTileDTO();
        MatchTile entity = new MatchTile();
        MatchTile savedEntity = new MatchTile();
        MatchTileDTO savedDTO = new MatchTileDTO();
        
        when(matchTileMapper.toEntity(dto)).thenReturn(entity);
        when(matchTileRepository.save(entity)).thenReturn(savedEntity);
        when(matchTileMapper.toDTO(savedEntity)).thenReturn(savedDTO);

        // Act
        MatchTileDTO result = matchTileService.saveAsDTO(dto);

        // Assert
        assertNotNull(result);
        verify(matchTileMapper).toEntity(dto);
        verify(matchTileRepository).save(entity);
        verify(matchTileMapper).toDTO(savedEntity);
    }

    // Test para updateAsDTO
    @ParameterizedTest
    @CsvSource({"1", "3"})
    void updateAsDTO_ShouldUpdateAndReturnDTO(Integer id) {
        // Arrange
        MatchTileDTO dto = new MatchTileDTO();
        MatchTile entity = new MatchTile();
        MatchTile existingEntity = new MatchTile();
        MatchTile updatedEntity = new MatchTile();
        MatchTileDTO updatedDTO = new MatchTileDTO();
        
        when(matchTileRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(matchTileMapper.toEntity(dto)).thenReturn(entity);
        when(matchTileRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(matchTileMapper.toDTO(updatedEntity)).thenReturn(updatedDTO);

        // Act
        MatchTileDTO result = matchTileService.updateAsDTO(id, dto);

        // Assert
        assertNotNull(result);
        verify(matchTileRepository).findById(id);
        verify(matchTileMapper).toEntity(dto);
        verify(matchTileRepository).save(existingEntity);
        verify(matchTileMapper).toDTO(updatedEntity);
    }

    // Test para delete (heredado de BaseService)
    @Test
    void delete_ShouldCallRepositoryDelete() {
        // Arrange
        Integer id = 1;
        MatchTile tile = new MatchTile();
        when(matchTileRepository.findById(id)).thenReturn(Optional.of(tile));

        // Act
        matchTileService.delete(id);

        // Assert
        verify(matchTileRepository).findById(id);
        verify(matchTileRepository).delete(tile);
    }
}