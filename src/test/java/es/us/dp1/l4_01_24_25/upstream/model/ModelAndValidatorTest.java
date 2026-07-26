package es.us.dp1.l4_01_24_25.upstream.model;

import es.us.dp1.l4_01_24_25.upstream.validation.NumberValidator;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelAndValidatorTest {

    @Test
    void testBaseEntity() {
        BaseEntity entity = new BaseEntity();
        assertTrue(entity.isNew());

        entity.setId(100);
        assertFalse(entity.isNew());
        assertEquals(100, entity.getId());
    }

    @Test
    void testNamedEntity() {
        NamedEntity entity = new NamedEntity();
        entity.setName("TestName");
        assertEquals("TestName", entity.getName());
        assertEquals("TestName", entity.toString());
    }

    @Test
    void testPerson() {
        Person person = new Person();
        person.setFirstName("John");
        person.setLastName("Doe");

        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
    }

    @Test
    void testNumberValidatorDigitLimits() {
        NumberValidator validator = new NumberValidator();
        ValidNumber annotation = mock(ValidNumber.class);
        when(annotation.min()).thenReturn(0.0);
        when(annotation.max()).thenReturn(10000.0);
        when(annotation.integer()).thenReturn(2);
        when(annotation.fraction()).thenReturn(1);

        validator.initialize(annotation);

        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(ctx.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(ctx);

        // Value with 3 integer digits when max is 2 -> invalid digits
        assertFalse(validator.isValid(123.0, ctx));

        // Value with 2 fraction digits when max is 1 -> invalid digits
        assertFalse(validator.isValid(12.34, ctx));

        // Valid value
        assertTrue(validator.isValid(12.3, ctx));
    }
}
