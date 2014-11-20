package com.artemis.annotations;

import com.artemis.EntityFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Invokes setter on component, instead of invoking fields. 
 * 
 * @see EntityFactory
 * @see Bind
 * @see Sticky
 */
@Retention(SOURCE)
@Target(METHOD)
@Documented
public @interface UseSetter {
	String value() default "";
}
