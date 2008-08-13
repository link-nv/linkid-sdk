/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;


/**
 * Gets thrown when an identity or authentication statement could not be decoded properly.
 *
 * @author fcorneli
 *
 */
@ApplicationException(rollback = true)
public class DecodingException extends SafeOnlineException {

    private static final long serialVersionUID = 1L;

}
