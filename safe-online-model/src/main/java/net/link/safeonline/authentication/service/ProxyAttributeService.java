package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


/**
 * Service that fetches that attribute values or locally or remotely.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ProxyAttributeService {

    /**
     * Fetches the device attributes for all registrations associated with the specified device mapping ID.
     * 
     * @param deviceMappingId
     * @param attributeName
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws AttributeUnavailableException
     */
    Object findDeviceAttributeValue(String deviceMappingId, String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException, AttributeUnavailableException;

    /**
     * Fetches an attribute from the specified subject ID.
     * 
     * @param userId
     * @param attributeName
     * @throws PermissionDeniedException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeUnavailableException
     */
    Object findAttributeValue(String userId, String attributeName) throws PermissionDeniedException,
            AttributeTypeNotFoundException, AttributeUnavailableException;
}
