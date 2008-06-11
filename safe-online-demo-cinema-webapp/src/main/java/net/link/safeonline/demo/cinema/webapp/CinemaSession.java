/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.webapp;

import org.apache.wicket.Request;
import org.apache.wicket.Session;

/**
 * <h2>{@link CinemaSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jun 10, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class CinemaSession extends Session {

    private static final long serialVersionUID = 1L;


    public CinemaSession(Request request) {

        super(request);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupFeedbackMessages() {

    }
    
    public static CinemaSession get() {

        return (CinemaSession) Session.get();
    }
}
