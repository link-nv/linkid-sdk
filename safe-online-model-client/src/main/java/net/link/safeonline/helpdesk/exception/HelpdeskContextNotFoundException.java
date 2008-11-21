/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.exception;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class HelpdeskContextNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
}