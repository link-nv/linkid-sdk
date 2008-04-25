package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
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
	 * Fetches a device attribute from the specified device user ID.
	 * 
	 * @param deviceUserId
	 * @param attributeName
	 * @throws AttributeTypeNotFoundException
	 * @throws PermissionDeniedException
	 */
	Object findDeviceAttributeValue(String deviceUserId, String attributeName)
			throws AttributeTypeNotFoundException, PermissionDeniedException;

	/**
	 * Fetches an attribute from the specified subject ID. This can be either a
	 * device or a subject attribute.
	 * 
	 * @param userId
	 * @param attributeName
	 * @throws PermissionDeniedException
	 * @throws AttributeTypeNotFoundException
	 */
	Object findAttributeValue(String userId, String attributeName)
			throws PermissionDeniedException, AttributeTypeNotFoundException;
}
