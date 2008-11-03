/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.List;

import javax.ejb.Local;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


import net.link.safeonline.SafeOnlineService;

@Local
public interface HelpdeskLog extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/HelpdeskLogBean/local";

    /*
     * Factories.
     */
    void helpdeskContextListFactory();

    void helpdeskLogListFactory();

    void helpdeskUserListFactory();

    void helpdeskUserContextListFactory();

    /*
     * Richfaces
     */
    List<String> autocomplete(Object event);

    List<String> autocompleteUser(Object event);

    /*
     * Validators
     */
    void validateId(FacesContext context, UIComponent toValidate, Object value);

    void validateUser(FacesContext context, UIComponent toValidate, Object value);

    /*
     * Accessors.
     */
    Long getSearchId();

    void setSearchId(Long searchId);

    String getSearchUserName();

    void setSearchUserName(String searchUserName);

    /*
     * Actions.
     */
    String search();

    String searchUser();

    String view();

    String viewUser();

    String removeLog();

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
