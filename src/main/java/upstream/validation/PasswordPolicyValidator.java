package upstream.validation;

import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator extends AbstractValidator<PasswordPolicy, CharSequence> {

    private int min;
    private int max;
    private boolean requireDigit;
    private boolean requireUppercase;

    @Override
    public void initialize(PasswordPolicy annotation) {
        super.initialize(annotation);
        this.min = annotation.min();
        this.max = annotation.max();
        this.requireDigit = annotation.requireDigit();
        this.requireUppercase = annotation.requireUppercase();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String password = value.toString();
        if (password.length() < min || password.length() > max) {
            addError(context, "Password must be between " + min + " and " + max + " characters");
            return false;
        }

        if (requireDigit && password.chars().noneMatch(Character::isDigit)) {
            addError(context, "Password must contain at least one numeric digit");
            return false;
        }

        if (requireUppercase && password.chars().noneMatch(Character::isUpperCase)) {
            addError(context, "Password must contain at least one uppercase letter");
            return false;
        }

        return true;
    }
}
