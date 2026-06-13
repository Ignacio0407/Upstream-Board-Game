package es.us.dp1.l4_01_24_25.upstream.coordinate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class CoordinateTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        this.validator = localValidatorFactoryBean;
    }

    @Test
    @DisplayName("Basic - Should create coordinate correctly")
    void shouldCreateCoordinate() {
        Coordinate coord = new Coordinate(1, 10);
        assertThat(coord.x()).isEqualTo(1);
        assertThat(coord.y()).isEqualTo(10);
    }

    @Test
    @DisplayName("Basic - Should handle equality and hashCode")
    void shouldHandleEquality() {
        Coordinate c1 = new Coordinate(1, 1);
        Coordinate c2 = new Coordinate(1, 1);
        Coordinate c3 = new Coordinate(2, 2);

        assertThat(c1).isEqualTo(c2);
        assertThat(c1).isNotEqualTo(c3);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("Basic - Should handle toString")
    void shouldHandleToString() {
        Coordinate coord = new Coordinate(1, 2);
        assertThat(coord.toString()).contains("x=1").contains("y=2");
    }

    @Test
    @DisplayName("Validation - Should validate when within bounds")
    void shouldValidateWhenWithinBounds() {
        Coordinate coord = new Coordinate(0, 0);
        Set<ConstraintViolation<Coordinate>> violations = validator.validate(coord);
        assertThat(violations).isEmpty();

        coord = new Coordinate(2, 25);
        violations = validator.validate(coord);
        assertThat(violations).isEmpty();

        coord = new Coordinate(1, 12);
        violations = validator.validate(coord);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation - Should not validate when X is out of bounds")
    void shouldNotValidateWhenXIsOutOfBounds() {
        // Below min
        Coordinate coord = new Coordinate(-1, 10);
        Set<ConstraintViolation<Coordinate>> violations = validator.validate(coord);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("x");

        // Above max
        coord = new Coordinate(3, 10);
        violations = validator.validate(coord);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("x");
    }

    @Test
    @DisplayName("Validation - Should not validate when Y is out of bounds")
    void shouldNotValidateWhenYIsOutOfBounds() {
        // Below min
        Coordinate coord = new Coordinate(1, -1);
        Set<ConstraintViolation<Coordinate>> violations = validator.validate(coord);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("y");

        // Above max
        coord = new Coordinate(1, 26);
        violations = validator.validate(coord);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("y");
    }

    @Test
    @DisplayName("Validation - Should allow null values (Handled by NumberValidator)")
    void shouldAllowNullValues() {
        Coordinate coord = new Coordinate(null, null);
        Set<ConstraintViolation<Coordinate>> violations = validator.validate(coord);
        assertThat(violations).isEmpty();
    }
}

