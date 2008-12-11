/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

/**
 * <h2>{@link OlasConfigurationService}<br>
 * <sub>OLAS Configuration Service API.</sub></h2>
 * 
 * <p>
 * OLAS Configuration Service API. This service should be used if OSGi bundles
 * wish to add and retrieve OLAS configuration information.
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface OlasConfigurationService {

	public void initConfigurationValue(String group, String name, Object value);

	public Object getConfigurationValue(String group, String name,
			Object defaultValue);

}
