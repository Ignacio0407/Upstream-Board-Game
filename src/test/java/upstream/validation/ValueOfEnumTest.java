package upstream.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import upstream.player.Color;

class ValueOfEnumTest {

    private Validator validator;

    private record ColorRecord(
        @ValueOfEnum(enumClass = Color.class)
        String color
    ) {}

    private record StrictColorRecord(
        @ValueOfEnum(enumClass = Color.class, ignoreCase = false)
        String color
    ) {}

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        this.validator = factory;
    }

    @ParameterizedTest
    @ValueSource(strings = {"RED", "PURPLE", "GREEN", "YELLOW", "WHITE", "red", "purple"})
    void shouldAcceptValidEnumValuesCaseInsensitive(String value) {
        Set<ConstraintViolation<ColorRecord>> violations = validator.validate(new ColorRecord(value));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BLUE", "BLACK", "INVALID_COLOR", "123"})
    void shouldRejectInvalidEnumValues(String value) {
        Set<ConstraintViolation<ColorRecord>> violations = validator.validate(new ColorRecord(value));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Value must be one of");
    }

    @ParameterizedTest
    @CsvSource({
        "RED, true",
        "red, false"
    })
    void shouldRespectCaseSensitivityWhenConfigured(String value, boolean valid) {
        Set<ConstraintViolation<StrictColorRecord>> violations = validator.validate(new StrictColorRecord(value));
        if (valid) {
            assertThat(violations).isEmpty();
        } else {
            assertThat(violations).hasSize(1);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true})
    void shouldAllowNullAsValidationResponsibilityBelongsToNotNull(boolean flag) {
        Set<ConstraintViolation<ColorRecord>> violations = validator.validate(new ColorRecord(null));
        assertThat(violations).isEmpty();
    }
}
