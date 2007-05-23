package net.link.safeonline;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

	String group() default "overall";

	String name() default "";

}
