/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.startup;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.demo.payment.service.InitializationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link PaymentStartupListener}<br>
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
public class PaymentStartupListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(PaymentStartupListener.class);


    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent sce) {

        try {
            ((InitializationService) new InitialContext().lookup(InitializationService.JNDI_BINDING)).buildEntities();
        } catch (NamingException e) {
            LOG.error("Couldn't find the payment initialization bean.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
