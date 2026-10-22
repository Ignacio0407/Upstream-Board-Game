package upstream.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a string does not contain HTML tags, javascript pseudo-protocols, or script injection patterns.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SafeTextValidator.class)
public @interface SafeText {

    String message() default "Field contains potentially unsafe HTML or script tags";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
