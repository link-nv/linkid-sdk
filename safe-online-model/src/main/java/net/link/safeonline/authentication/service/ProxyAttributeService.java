package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Service that fetches that attribute values or locally or remotely or externally.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ProxyAttributeService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ProxyAttributeServiceBean/local";


    /**
     * Fetches an attribute from the specified subject ID.
     * 
     * @param userId
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    Object findAttributeValue(String userId, String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException, SubjectNotFoundException;
}
