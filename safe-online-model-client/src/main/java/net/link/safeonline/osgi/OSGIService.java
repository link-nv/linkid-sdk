/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import net.link.safeonline.authentication.exception.SafeOnlineResourceException;


/**
 * <h2>{@link OSGIService}<br>
 * <sub>OSGI helper service to manage OSGI services life cycle.</sub></h2>
 * 
 * <p>
 * OSGI helper service to manage OSGI services life cycle.
 * </p>
 * 
 * <p>
 * <i>Feb 23, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public interface OSGIService {

    /**
     * Returns instance of an OSGI service.
     * 
     * @throws SafeOnlineResourceException
     */
    public Object getService()
            throws SafeOnlineResourceException;

    /**
     * Free's up instance of OSGI service
     */
    public void ungetService();
}
