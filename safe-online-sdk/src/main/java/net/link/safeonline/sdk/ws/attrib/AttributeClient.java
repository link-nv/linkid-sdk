/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import java.net.ConnectException;
import java.util.Map;

import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;

/**
 * Interface for attribute client. Via components implementing this interface
 * applications can retrieve attributes for subjects. Applications can only
 * retrieve attribute values for which the user confirmed the corresponding
 * application identity.
 * 
 * @author fcorneli
 * 
 */
public interface AttributeClient extends MessageAccessor {

	/**
	 * Gives back the attribute value of a single attribute of the given
	 * subject. The type of the value returned depends on the datatype of the
	 * corresponding attribute type.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws RequestDeniedException
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 */
	Object getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, RequestDeniedException,
			ConnectException;

	/**
	 * Gives back attribute values via the map of attributes. The map should
	 * hold the requested attribute names as keys. The method will fill in the
	 * corresponding values.
	 * 
	 * @param subjectLogin
	 * @param attributes
	 * @throws AttributeNotFoundException
	 * @throws RequestDeniedException
	 * @throws ConnectException
	 */
	void getAttributeValues(String subjectLogin, Map<String, Object> attributes)
			throws AttributeNotFoundException, RequestDeniedException,
			ConnectException;

	/**
	 * Gives back a map of attributes for the given subject that this
	 * application is allowed to read.
	 * 
	 * @param subjectLogin
	 * @return
	 * @throws RequestDeniedException
	 * @throws ConnectException
	 * @throws AttributeNotFoundException
	 */
	Map<String, Object> getAttributeValues(String subjectLogin)
			throws RequestDeniedException, ConnectException,
			AttributeNotFoundException;
}
