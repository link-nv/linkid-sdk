/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.shared.SharedConstants;

@ApplicationException(rollback = true)
public class DigipassException extends SafeOnlineException {

	public DigipassException(String message) {
		super(message, SharedConstants.UNDEFINED_ERROR);
	}

	private static final long serialVersionUID = 1L;

}
