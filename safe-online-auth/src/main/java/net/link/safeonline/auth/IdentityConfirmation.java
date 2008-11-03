/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.data.AttributeDO;


@Local
public interface IdentityConfirmation {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "IdentityConfirmationBean/local";

    /*
     * Accessors
     */
    String getApplicationUrl();

    /*
     * Actions.
     */
    String agree() throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException,
                  AttributeTypeNotFoundException, SubscriptionNotFoundException, AttributeUnavailableException;

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Factory.
     */
    List<AttributeDO> identityConfirmationListFactory() throws SubscriptionNotFoundException, ApplicationNotFoundException,
                                                       ApplicationIdentityNotFoundException;

    List<AttributeDO> identityUnavailableListFactory() throws PermissionDeniedException, AttributeTypeNotFoundException,
                                                      ApplicationNotFoundException, ApplicationIdentityNotFoundException;
}
