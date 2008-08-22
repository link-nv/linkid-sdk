/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import java.util.List;

import net.link.safeonline.osgi.plugin.Attribute;
import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * <h2>{@link OlasAttributeServiceImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OlasAttributeServiceImpl implements OlasAttributeService {

    /**
     * {@inheritDoc}
     */
    public List<Attribute> getAttribute(String userId, String attributeName) throws AttributeTypeNotFoundException,
            AttributeNotFoundException, UnsupportedDataTypeException {

        OSGIAttributeService attributeService = EjbUtils.getEJB(OSGIAttributeService.JNDI_BINDING,
                OSGIAttributeService.class);
        try {
            return attributeService.getAttribute(userId, attributeName);
        } catch (net.link.safeonline.authentication.exception.AttributeTypeNotFoundException e) {
            throw new AttributeTypeNotFoundException(e.getMessage());
        } catch (net.link.safeonline.authentication.exception.AttributeNotFoundException e) {
            throw new AttributeNotFoundException(e.getMessage());
        }
    }

}
