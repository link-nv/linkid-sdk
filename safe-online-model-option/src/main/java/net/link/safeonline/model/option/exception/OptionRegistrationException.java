/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.shared.SharedConstants;

/**
 * <h2>{@link OptionRegistrationException}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@ApplicationException(rollback = true)
public class OptionRegistrationException extends SafeOnlineException {

	private static final long serialVersionUID = 1L;

	public OptionRegistrationException() {
		super(null, SharedConstants.PERMISSION_DENIED_ERROR);
	}
}
