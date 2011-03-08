/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.configuration;

/**
 * <h2>{@link LinkIDConfigurationService}<br>
 * <sub>linkID Configuration Service API.</sub></h2>
 *
 * <p>
 * linkID Configuration Service API. This service should be used if OSGi bundles wish to add and retrieve linkID configuration information.
 * </p>
 *
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
public interface LinkIDConfigurationService {

    public void initConfigurationValue(String group, String name, Object value);

    public Object getConfigurationValue(String group, String name, Object defaultValue);
}
