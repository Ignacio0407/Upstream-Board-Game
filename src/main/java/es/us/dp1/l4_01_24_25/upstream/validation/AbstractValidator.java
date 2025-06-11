package es.us.dp1.l4_01_24_25.upstream.validation;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public abstract class AbstractValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    private A annotation;

    @Override
    public void initialize(A constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    protected A getAnnotation() {
        return annotation;
    }

    protected void addError(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}