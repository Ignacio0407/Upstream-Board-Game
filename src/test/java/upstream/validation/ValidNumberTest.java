package upstream.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class ValidNumberTest {

    private Validator validator;

    // Auxiliary class with different configurations of @ValidNumber
    private record BoundedField(
        @ValidNumber(min = 0, max = 10) Integer value
    ) {}

    private record UnboundedField(
        @ValidNumber Integer value
    ) {}

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        this.validator = factory;
    }

    @ParameterizedTest
    @CsvSource({
        "0,  true",   // Inferior limit
        "5,  true",   // Central value
        "10, true",   // Superior limit
        "-1, false",  // Below minimum
        "11, false"   // Above maximum
    })
    void shouldValidateBoundedNumber(int value, boolean valid) {
        var violations = validator.validate(new BoundedField(value));
        if (valid) assertThat(violations).isEmpty();
        else       assertThat(violations).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"0", "999999", "-999999"})
    void shouldAcceptAnyValueWhenUnbounded(int value) {
        Set<ConstraintViolation<UnboundedField>> violations = validator.validate(new UnboundedField(value));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldAllowNullRegardlessOfBounds(boolean bounded) {
        // @ValidNumber does not impose @NotNull — null always pass
        Set<?> violations = bounded
            ? validator.validate(new BoundedField(null))
            : validator.validate(new UnboundedField(null));
        assertThat(violations).isEmpty();
    }
}