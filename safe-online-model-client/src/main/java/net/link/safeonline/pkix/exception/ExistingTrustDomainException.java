/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.ExistingException;


@ApplicationException(rollback = true)
public class ExistingTrustDomainException extends ExistingException {

    private static final long serialVersionUID = 1L;

}