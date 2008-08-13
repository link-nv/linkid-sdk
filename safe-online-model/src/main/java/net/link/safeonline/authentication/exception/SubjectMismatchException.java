/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Liimport javax.ejb.ApplicationException;
N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class SubjectMismatchException extends SafeOnlineException {

    private static final long serialVersionUID = 1L;

}
