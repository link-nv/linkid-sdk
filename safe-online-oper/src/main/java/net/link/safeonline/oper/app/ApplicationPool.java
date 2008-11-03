/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;


@Local
public interface ApplicationPool extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationPoolBean/local";

    /*
     * Factory
     */
    void applicationPoolListFactory();

    void applicationPoolApplicationListFactory();

    void applicationPoolElementsFactory();

    List<SelectItem> getApplicationList();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    String getName();

    void setName(String name);

    Long getSsoTimeout();

    void setSsoTimeout(Long ssoTimeout);

    /*
     * Actions.
     */
    String add() throws ApplicationPoolNotFoundException, ApplicationNotFoundException;

    String remove() throws ApplicationPoolNotFoundException;

    String save() throws ApplicationPoolNotFoundException, ApplicationNotFoundException;

    String view();

    String edit();
}
