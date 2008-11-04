/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.plugin.Attribute;
import net.link.safeonline.osgi.plugin.exception.UnsupportedDataTypeException;


/**
 * <h2>{@link OSGIAttributeService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Aug 18, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface OSGIAttributeService {

    public final String JNDI_BINDING = "SafeOnline/OSGIAttributeServiceBean/local";


    /**
     * 
     * TODO
     * 
     * @throws UnsupportedDataTypeException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    public List<Attribute> getAttribute(String userId, String attributeName)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, UnsupportedDataTypeException, AttributeUnavailableException,
            SubjectNotFoundException;

}
