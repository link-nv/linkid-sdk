/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Attribute Service. To be used by Olas nodes to retrieve attributes of their users.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface NodeAttributeService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeAttributeServiceBean/local";


    /**
     * Gives back the value of an attribute of a certain subject.
     * 
     * <p>
     * The returned object can be a {@link String} or {@link Boolean}, ... depending on the actual datatype used by corresponding attribute
     * type of the requested attribute. In case of a multivalued attribute the returned object will be an array. In case of a compounded a
     * Map will be returned. In case of a multivalued compounded an array of Maps will be returned.
     * </p>
     * 
     * @param subjectId
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     */
    Object getAttributeValue(String subjectId, String attributeName)
            throws PermissionDeniedException, SubjectNotFoundException, AttributeTypeNotFoundException, AttributeUnavailableException;
}
