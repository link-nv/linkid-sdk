/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationAdminException;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface ApplicationOwner {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "ApplicationOwnerBean/local";

    /*
     * Factory.
     */
    void applicationOwnerListFactory();

    void applicationListFactory();

    /*
     * Accessors.
     */
    String getLogin();

    void setLogin(String login);

    String getName();

    void setName(String name);

    /*
     * Actions.
     */
    String add() throws SubjectNotFoundException, ExistingApplicationOwnerException, ExistingApplicationAdminException;

    String remove() throws SubscriptionNotFoundException, SubjectNotFoundException, ApplicationOwnerNotFoundException,
                   PermissionDeniedException;

    String view();

    String viewapp();

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
