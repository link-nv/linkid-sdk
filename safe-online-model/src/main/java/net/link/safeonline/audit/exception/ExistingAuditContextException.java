/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.authentication.exception.ExistingException;


@ApplicationException(rollback = true)
public class ExistingAuditContextException extends ExistingException {

    private static final long serialVersionUID = 1L;

    private final long        auditContextId;


    public ExistingAuditContextException(long auditContextId) {

        this.auditContextId = auditContextId;
    }

    public long getAuditContextId() {

        return this.auditContextId;
    }
}
