/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin;

import java.util.List;

import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;

/**
 * <h2>{@link PluginAttributeService}<br>
 * <sub>Plugin Attribute service API. </sub></h2>
 * 
 * <p>
 * Plugin Attribute service API.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface PluginAttributeService {

	public List<Attribute> getAttribute(String userId, String attributeName,
			String configuration) throws UnsupportedDataTypeException,
			AttributeNotFoundException, AttributeTypeNotFoundException;

}
