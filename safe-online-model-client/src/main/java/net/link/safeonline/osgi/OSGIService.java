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
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
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
     * TODO
     */
    public Object getService()
            throws SafeOnlineResourceException;

    /**
     * TODO
     */
    public void ungetService();
}
