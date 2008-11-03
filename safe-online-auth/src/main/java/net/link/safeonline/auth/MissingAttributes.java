/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface MissingAttributes {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "MissingAttributesBean/local";

    /*
     * Factories.
     */
    void missingAttributeListFactory() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException,
                                      PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException;

    void optionalAttributeListFactory() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException,
                                       PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException;

    /*
     * Actions.
     */
    String save();

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
