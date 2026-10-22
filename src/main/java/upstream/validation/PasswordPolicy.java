package upstream.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a password satisfies configurable complexity and length requirements.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface PasswordPolicy {

    int min() default 6;

    int max() default 128;

    boolean requireDigit() default false;

    boolean requireUppercase() default false;

    String message() default "Password must meet policy requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
