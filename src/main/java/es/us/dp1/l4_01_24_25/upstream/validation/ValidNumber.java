package es.us.dp1.l4_01_24_25.upstream.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/*
 * ValidNumber.java
 *
 * Copyright (C) 2012-2025 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented

@Constraint(validatedBy = NumberValidator.class)

public @interface ValidNumber {

	// Custom properties ------------------------------------------------------

	double min() default Double.NEGATIVE_INFINITY;
	double max() default Double.POSITIVE_INFINITY;
	int integer() default 6;
	int fraction() default 2;

	// Standard validation properties -----------------------------------------

	String message() default "{placeholder}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

}

