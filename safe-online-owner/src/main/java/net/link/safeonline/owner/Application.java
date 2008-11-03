/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface Application extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationBean/local";

    /*
     * Accessors
     */

    /*
     * Factories
     */
    void applicationListFactory() throws ApplicationOwnerNotFoundException;

    void usageAgreementListFactory() throws ApplicationNotFoundException, PermissionDeniedException;

    /*
     * Actions
     */
    String view() throws ApplicationNotFoundException, PermissionDeniedException, ApplicationIdentityNotFoundException;

    String edit();

    String save() throws ApplicationNotFoundException, PermissionDeniedException;

    String viewStats();

    void allowedDevices();

    String viewUsageAgreement();

    String editUsageAgreement() throws ApplicationNotFoundException, PermissionDeniedException;

    /*
     * Lifecycle
     */
    void destroyCallback();

}
