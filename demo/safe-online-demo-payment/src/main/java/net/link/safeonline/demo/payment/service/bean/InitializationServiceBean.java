/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.service.bean;

import javax.ejb.Stateless;

import net.link.safeonline.demo.payment.service.InitializationService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link InitializationServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
@Stateless
@LocalBinding(jndiBinding = InitializationService.JNDI_BINDING)
public class InitializationServiceBean extends AbstractPaymentServiceBean implements InitializationService {

    /**
     * {@inheritDoc}
     */
    public void buildEntities() {

    }
}
