/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;


/**
 * Interface for SAML authority service.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SamlAuthorityService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "SamlAuthorityServiceBean/local";


    /**
     * Gives back the name of the SAML assertion issuer.
     * 
     */
    String getIssuerName();

    /**
     * Gives back the validity of the authentication assertions issued by this party.
     * 
     */
    int getAuthnAssertionValidity();
}
