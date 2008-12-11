/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeTypeNotFoundException;
import net.link.safeonline.osgi.exception.AttributeUnavailableException;
import net.link.safeonline.osgi.exception.SubjectNotFoundException;


/**
 * <h2>{@link OlasAttributeService}<br>
 * <sub>OLAS Attribute Service API.</sub></h2>
 * 
 * <p>
 * OLAS Attribute Service API. This service should be used if OSGi attribute plugins wish to retrieve OLAS attributes.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface OlasAttributeService {

    /**
     * Returns the attribute from OLAS for the specified attribute name from the specified user.
     * 
     * @param userId
     * @param attributeName
     * @return attribute view
     * @throws AttributeTypeNotFoundException
     * @throws AttributeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    public Object getAttribute(String userId, String attributeName)
            throws AttributeTypeNotFoundException, AttributeNotFoundException, AttributeUnavailableException, SubjectNotFoundException;

}
