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
import net.link.safeonline.osgi.plugin.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.plugin.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;


/**
 * <h2>{@link PluginAttributeService}<br>
 * <sub>Plugin Attribute service API. </sub></h2>
 * 
 * <p>
 * Plugin Attribute service API. OSGi attribute plugins should implement this interface.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface PluginAttributeService {

    /**
     * Returns the attribute view for the specified attribute name from the specified user with the implementing OSGi attribute plugin given
     * the specified configuration.
     * 
     * @param userId
     * @param attributeName
     * @param configuration
     * @return attribute view
     * @throws UnsupportedDataTypeException
     * @throws AttributeNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    public List<Attribute> getAttribute(String userId, String attributeName, String configuration)
            throws UnsupportedDataTypeException, AttributeNotFoundException, AttributeTypeNotFoundException, AttributeUnavailableException,
            SubjectNotFoundException;

}
