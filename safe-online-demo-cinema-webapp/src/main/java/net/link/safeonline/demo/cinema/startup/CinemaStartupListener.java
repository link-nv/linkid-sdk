/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.startup;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.demo.cinema.service.InitializationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link CinemaStartupListener}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Jun 23, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class CinemaStartupListener implements ServletContextListener {

    private static final Log LOG = LogFactory
                                         .getLog(CinemaStartupListener.class);
    
    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent sce) {

        try {
            ((InitializationService) new InitialContext()
                    .lookup(InitializationService.BINDING)).buildEntities();
        } catch (NamingException e) {
            LOG.error("Couldn't find the cinema initialization bean.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent sce) {

    }
}