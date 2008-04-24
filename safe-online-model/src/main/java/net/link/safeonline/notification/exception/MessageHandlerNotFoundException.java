/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.shared.SharedConstants;

@ApplicationException(rollback = true)
public class MessageHandlerNotFoundException extends SafeOnlineException {

	private static final long serialVersionUID = 1L;

	public MessageHandlerNotFoundException(String topic) {
		super("No message handler found for topic: " + topic,
				SharedConstants.UNDEFINED_ERROR);
	}
}