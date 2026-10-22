package upstream.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class PasswordPolicyTest {

    private Validator validator;

    private record BasicPasswordRecord(
        @PasswordPolicy(min = 6)
        String password
    ) {}

    private record StrictPasswordRecord(
        @PasswordPolicy(min = 8, requireDigit = true, requireUppercase = true)
        String password
    ) {}

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        this.validator = factory;
    }

    @ParameterizedTest
    @CsvSource({
        "123456, true",
        "password, true",
        "short, false",
        "123, false"
    })
    void shouldValidateBasicPasswordLength(String password, boolean valid) {
        Set<ConstraintViolation<BasicPasswordRecord>> violations = validator.validate(new BasicPasswordRecord(password));
        if (valid) {
            assertThat(violations).isEmpty();
        } else {
            assertThat(violations).hasSize(1);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "ValidPass1, true",
        "validpass1, false", // Missing uppercase
        "ValidPassWord, false", // Missing digit
        "Pass1, false" // Too short (< 8)
    })
    void shouldValidateStrictPasswordComplexity(String password, boolean valid) {
        Set<ConstraintViolation<StrictPasswordRecord>> violations = validator.validate(new StrictPasswordRecord(password));
        if (valid) {
            assertThat(violations).isEmpty();
        } else {
            assertThat(violations).isNotEmpty();
        }
    }

    @Test
    void shouldAllowNull() {
        Set<ConstraintViolation<BasicPasswordRecord>> violations = validator.validate(new BasicPasswordRecord(null));
        assertThat(violations).isEmpty();
    }
}
