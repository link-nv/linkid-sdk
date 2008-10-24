/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.link.safeonline.validation.validator.Validator;


/**
 * Validator meta-annotation.
 * 
 * @author fcorneli
 * 
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatorAnnotation {

    /**
     * The validator class that implements the validator semantics.
     * 
     */
    @SuppressWarnings("unchecked")
    Class<? extends Validator> value();
}
