/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import javax.ejb.Local;


/**
 * <h2>{@link OSGIConfigurationService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface OSGIConfigurationService {

    public final String JNDI_BINDING = "SafeOnline/OSGIConfigurationServiceBean/local";


    Object getConfigurationValue(String group, String name);

    void initConfigurationValue(String group, String name, Object value);
}
