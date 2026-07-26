package es.us.dp1.l4_01_24_25.upstream.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractValidatorTest {

    static class ConcreteValidator extends AbstractValidator<ValidNumber, Integer> {
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return true;
        }

        public ValidNumber getAnno() {
            return getAnnotation();
        }

        public void callAddError(ConstraintValidatorContext ctx, String msg) {
            addError(ctx, msg);
        }
    }

    @Test
    void testAbstractValidatorMethods() {
        ConcreteValidator validator = new ConcreteValidator();
        ValidNumber annotation = mock(ValidNumber.class);

        validator.initialize(annotation);
        assertEquals(annotation, validator.getAnno());

        ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(ctx.buildConstraintViolationWithTemplate("error message")).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(ctx);

        validator.callAddError(ctx, "error message");

        verify(ctx).disableDefaultConstraintViolation();
        verify(ctx).buildConstraintViolationWithTemplate("error message");
        verify(builder).addConstraintViolation();
    }
}
