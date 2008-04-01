package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;

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
	 * @throws SubjectNotFoundException
	 * @throws AttributeNotFoundException
	 */
	Object findDeviceAttributeValue(String deviceUserId, String attributeName)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException;

	/**
	 * Fetches an attribute from the specified subject ID. This can be either a
	 * device or a subject attribute.
	 * 
	 * @param userId
	 * @param attributeName
	 * @throws PermissionDeniedException
	 * @throws SubjectNotFoundException
	 * @throws AttributeTypeNotFoundException
	 */
	Object findAttributeValue(String userId, String attributeName)
			throws PermissionDeniedException, SubjectNotFoundException,
			AttributeTypeNotFoundException;
}
