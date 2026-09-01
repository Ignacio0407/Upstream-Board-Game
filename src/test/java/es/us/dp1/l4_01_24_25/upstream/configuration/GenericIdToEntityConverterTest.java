package es.us.dp1.l4_01_24_25.upstream.configuration;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericIdToEntityConverterTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GenericIdToEntityConverter converter;

    @Test
    void testGetConvertibleTypes() {
        Set<ConvertiblePair> types = converter.getConvertibleTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
    }

    @Test
    void testMatches() {
        TypeDescriptor numSource = TypeDescriptor.valueOf(Integer.class);
        TypeDescriptor strSource = TypeDescriptor.valueOf(String.class);
        TypeDescriptor targetBaseEntity = TypeDescriptor.valueOf(BaseEntity.class);
        TypeDescriptor targetOther = TypeDescriptor.valueOf(String.class);

        assertTrue(converter.matches(numSource, targetBaseEntity));
        assertTrue(converter.matches(strSource, targetBaseEntity));
        assertFalse(converter.matches(numSource, targetOther));
    }

    @Test
    void testConvert_NullSourceOrNullEntityManager() {
        TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        TypeDescriptor targetType = TypeDescriptor.valueOf(BaseEntity.class);

        // Null source
        assertNull(converter.convert(null, sourceType, targetType));

        // Null entityManager
        GenericIdToEntityConverter nullEmConverter = new GenericIdToEntityConverter();
        assertNull(nullEmConverter.convert("1", sourceType, targetType));
    }

    @Test
    void testConvert_FoundAndNotFound() {
        TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        TypeDescriptor targetType = TypeDescriptor.valueOf(BaseEntity.class);

        BaseEntity dummyEntity = new BaseEntity();
        dummyEntity.setId(10);

        when(entityManager.find(BaseEntity.class, 10)).thenReturn(dummyEntity);

        Object found = converter.convert("10", sourceType, targetType);
        assertEquals(dummyEntity, found);

        when(entityManager.find(BaseEntity.class, 99)).thenReturn(null);
        Object notFound = converter.convert("99", sourceType, targetType);
        assertNull(notFound);
    }
}
