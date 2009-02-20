/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;


/**
 * <h2>{@link OlasLogServiceImpl}<br>
 * <sub>Implements an OLAS OSGi logging service</sub></h2>
 * 
 * <p>
 * This service implements a logging service.
 * </p>
 * 
 * <p>
 * <i>Feb 18, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class OlasLogServiceImpl implements LogService {

    private static final Log LOG    = LogFactory.getLog(OlasLogServiceImpl.class);

    private String           logger = "";


    public OlasLogServiceImpl(String logger) {

        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public void log(int level, String message) {

        log(null, level, message, null);
    }

    /**
     * {@inheritDoc}
     */
    public void log(int level, String message, Throwable throwable) {

        log(null, level, message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void log(ServiceReference service, int level, String message) {

        log(service, level, message, null);
    }

    /**
     * {@inheritDoc}
     */
    public void log(ServiceReference service, int level, String message, Throwable throwable) {

        String finalMessage;
        String throwableMessage;

        if (null == message) {
            finalMessage = "empty log action";
        } else {
            finalMessage = message;
        }

        if (null == throwable) {
            throwableMessage = "(No throwable)";
        } else {
            throwableMessage = "(throwable msg: " + throwable.getMessage() + ")";
        }

        String logString = "[" + logger + "] " + finalMessage + " " + throwableMessage;
        if (LogService.LOG_DEBUG == level) {
            LOG.debug(logString);
        } else if (LogService.LOG_ERROR == level) {
            LOG.error(logString);
        } else if (LogService.LOG_INFO == level) {
            LOG.info(logString);
        } else if (LogService.LOG_WARNING == level) {
            LOG.warn(logString);
        }
    }

}
