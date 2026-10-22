package upstream.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidatorContext;

public class SafeTextValidator extends AbstractValidator<SafeText, CharSequence> {

    // Matches any HTML tag (including open/close/self-closing) or javascript: pseudo-protocol
    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
        "<\\s*[a-zA-Z\\/!][^>]*>|javascript\\s*:",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String text = value.toString();
        if (UNSAFE_PATTERN.matcher(text).find()) {
            addError(context, "Input contains disallowed HTML tags or scripts");
            return false;
        }

        return true;
    }
}
