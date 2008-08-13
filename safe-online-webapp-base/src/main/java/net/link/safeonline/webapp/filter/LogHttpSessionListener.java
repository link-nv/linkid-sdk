/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LogHttpSessionListener implements HttpSessionListener {

    private static final Log LOG = LogFactory.getLog(LogHttpSessionListener.class);


    public void sessionCreated(HttpSessionEvent event) {

        LOG.debug("session created: " + event.getSession().getId());
    }

    public void sessionDestroyed(HttpSessionEvent event) {

        LOG.debug("session destroyed: " + event.getSession().getId());
    }
}
