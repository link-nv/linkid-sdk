/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * <h2>{@link OSGIAttributeService}<br>
 * <sub>EJB service used by the OSGI {@link OlasAttributeService}.</sub></h2>
 * 
 * <p>
 * EJB service used by the OSGI {@link OlasAttributeService}.
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
     * Returns attribute for specified subject with userId and specified attribute name
     * 
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    public Object getAttribute(String userId, String attributeName)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException;

}
