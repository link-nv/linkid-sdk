/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;


/**
 * Datatype mismatch exception. Gets thrown when a value datatype does not match with the expected datatype. For example: an application
 * wants to set the value of boolean-datatyped attribute with an object of type string.
 * 
 * @author fcorneli
 * 
 */
@ApplicationException(rollback = true)
public class DatatypeMismatchException extends SafeOnlineException {

    private static final long serialVersionUID = 1L;
}
