/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.entity.audit.SecurityThreatType;


@ApplicationException(rollback = true)
public class SafeOnlineSecurityException extends SafeOnlineException {

    private static final long        serialVersionUID = 1L;

    private final SecurityThreatType securityThreat;

    private final String             targetPrincipal;


    public SafeOnlineSecurityException(SecurityThreatType securityThreat) {

        this.securityThreat = securityThreat;
        targetPrincipal = null;
    }

    public SafeOnlineSecurityException(SecurityThreatType securityThreat, String targetPrincipal) {

        this.securityThreat = securityThreat;
        this.targetPrincipal = targetPrincipal;
    }

    public SecurityThreatType getSecurityThreat() {

        return securityThreat;
    }

    public String getTargetPrincipal() {

        return targetPrincipal;
    }

}
