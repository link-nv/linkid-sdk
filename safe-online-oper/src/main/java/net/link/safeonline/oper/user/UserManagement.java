/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.user;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.RoleNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.ctrl.HistoryMessage;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;


@Local
public interface UserManagement extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/UserManagementBean/local";

    /*
     * Accessors.
     */
    String getUser();

    void setUser(String user);

    List<String> getRoles();

    void setRoles(List<String> roles);

    List<HistoryMessage> getHistoryList();

    List<SubscriptionEntity> getSubscriptionList();

    List<DeviceRegistrationDO> getDeviceRegistrationList();

    List<AttributeDO> getAttributeList();

    /*
     * Actions.
     */
    String search() throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException;

    String save() throws SubjectNotFoundException, RoleNotFoundException;

    String remove();

    String removeConfirm() throws SubjectNotFoundException, SubscriptionNotFoundException, MessageHandlerNotFoundException;

    String removeCancel();

    /*
     * Richfaces.
     */
    List<String> autocompleteUser(Object event);

    /*
     * Lifecycle.
     */
    void destroyCallback();

    void postConstructCallback();

    /*
     * Factories.
     */
    List<SelectItem> availableRolesFactory();
}
