/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.link.safeonline.keystore.AbstractKeyStore;


/**
 * <h2>{@link OlasService}<br>
 * <sub>Annotation that causes the OLAS service that matches the field's type to get injected.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 15, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OlasService {

    /**
     * The {@link AbstractKeyStore} for this application.
     */
    Class<? extends AbstractKeyStore> keyStore();
}
