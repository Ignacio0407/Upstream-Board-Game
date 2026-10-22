package upstream.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidatorContext;

public class ValueOfEnumValidator extends AbstractValidator<ValueOfEnum, CharSequence> {

    private List<String> acceptedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(ValueOfEnum annotation) {
        super.initialize(annotation);
        this.ignoreCase = annotation.ignoreCase();
        this.acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String strValue = value.toString().trim();
        boolean matches = ignoreCase
                ? acceptedValues.stream().anyMatch(val -> val.equalsIgnoreCase(strValue))
                : acceptedValues.contains(strValue);

        if (!matches) {
            addError(context, "Value must be one of: " + acceptedValues);
            return false;
        }

        return true;
    }
}
