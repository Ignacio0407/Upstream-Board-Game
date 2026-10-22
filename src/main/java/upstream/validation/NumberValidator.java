package upstream.validation;

import jakarta.validation.ConstraintValidatorContext;

public class NumberValidator extends AbstractValidator<ValidNumber, Number> {

    private double minimum;
    private double maximum;
    private int integer;
    private int fraction;

    @Override
    public void initialize(ValidNumber constraintAnnotation) {
        super.initialize(constraintAnnotation);
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
            addError(context, "Value must be between " + minimum + " and " + maximum);
            return false;
        }

        // Implementación simplificada para contar dígitos
        String[] parts = String.valueOf(doubleValue).split("\\.");
        int integerDigits = parts[0].replace("-", "").length();
        int fractionDigits = (parts.length > 1 && !parts[1].equals("0")) ? parts[1].length() : 0;

        boolean isValidDigits = integerDigits <= this.integer && fractionDigits <= this.fraction;

        if (!isValidDigits) {
            addError(context, "Number must have at most " + integer + 
                " integer digits and " + fraction + " fraction digits");
            return false;
        }

        return true;
    }
}