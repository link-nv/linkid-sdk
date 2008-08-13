/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.shared.SharedConstants;


@ApplicationException(rollback = true)
public class PermissionDeniedException extends SafeOnlineException {

    private static final long serialVersionUID = 1L;

    private String            resourceMessage;

    private Object[]          resourceArgs;


    public PermissionDeniedException(String message) {

        super(message, SharedConstants.PERMISSION_DENIED_ERROR);
    }

    public PermissionDeniedException(String message, String resourceMessage, Object... resourceArgs) {

        super(message, SharedConstants.PERMISSION_DENIED_ERROR);
        this.resourceMessage = resourceMessage;
        this.resourceArgs = resourceArgs;
    }

    public String getResourceMessage() {

        return this.resourceMessage;
    }

    public Object[] getResourceArgs() {

        return this.resourceArgs;
    }
}
