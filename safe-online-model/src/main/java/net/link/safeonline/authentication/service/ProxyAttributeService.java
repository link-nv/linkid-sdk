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

	Object getAttributeValue(String userId, String attributeName)
			throws AttributeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, AttributeTypeNotFoundException;
}
