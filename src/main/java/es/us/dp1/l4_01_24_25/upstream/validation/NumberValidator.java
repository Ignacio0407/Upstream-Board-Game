package es.us.dp1.l4_01_24_25.upstream.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumberValidator implements ConstraintValidator<ValidNumber, Number> {

    private double minimum;
    private double maximum;
    private int integer;
    private int fraction;

    @Override
    public void initialize(ValidNumber constraintAnnotation) {
        this.minimum = constraintAnnotation.min();
        this.maximum = constraintAnnotation.max();
        this.integer = constraintAnnotation.integer();
        this.fraction = constraintAnnotation.fraction();
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        double doubleValue = value.doubleValue();
        boolean isValidRange = this.minimum <= doubleValue && doubleValue <= this.maximum;

        if (!isValidRange) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Value must be between " + minimum + " and " + maximum)
                   .addConstraintViolation();
            return false;
        }

        // Implementación simplificada para contar dígitos
        String[] parts = String.valueOf(doubleValue).split("\\.");
        int integerDigits = parts[0].replace("-", "").length();
        int fractionDigits = parts.length > 1 ? parts[1].length() : 0;

        boolean isValidDigits = integerDigits <= this.integer && fractionDigits <= this.fraction;

        if (!isValidDigits) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Number must have at most " + integer + 
                " integer digits and " + fraction + " fraction digits")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}