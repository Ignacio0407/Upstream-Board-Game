package upstream.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class SafeTextTest {

    private Validator validator;

    private record CommentRecord(
        @SafeText
        String comment
    ) {}

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        this.validator = factory;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Hello world!",
        "Nice match! Good game.",
        "123 numbers and punctuation: !?@#$%^&*()",
        "Salmon moved to tile 3"
    })
    void shouldAcceptSafePlainText(String text) {
        Set<ConstraintViolation<CommentRecord>> violations = validator.validate(new CommentRecord(text));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>alert('xss')</script>",
        "<img src=x onerror=alert(1)>",
        "<a href=\"javascript:void(0)\">click</a>",
        "Hello <b>bold</b> text",
        "<iframe src=\"http://malicious.com\"></iframe>",
        "javascript:alert(1)"
    })
    void shouldRejectHtmlAndScriptInjections(String text) {
        Set<ConstraintViolation<CommentRecord>> violations = validator.validate(new CommentRecord(text));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Input contains disallowed HTML tags or scripts");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true})
    void shouldAllowNull(boolean flag) {
        Set<ConstraintViolation<CommentRecord>> violations = validator.validate(new CommentRecord(null));
        assertThat(violations).isEmpty();
    }
}
