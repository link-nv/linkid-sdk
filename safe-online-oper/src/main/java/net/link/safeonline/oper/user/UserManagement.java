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

import net.link.safeonline.ctrl.HistoryMessage;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceMappingDO;
import net.link.safeonline.entity.SubscriptionEntity;

@Local
public interface UserManagement {

	/*
	 * Accessors.
	 */
	String getUser();

	void setUser(String user);

	List<String> getRoles();

	void setRoles(List<String> roles);

	List<HistoryMessage> getHistoryList();

	List<SubscriptionEntity> getSubscriptionList();

	List<DeviceMappingDO> getDeviceRegistrationList();

	List<AttributeDO> getAttributeList();

	/*
	 * Actions.
	 */
	String search();

	String save();

	String remove();

	String removeConfirm();

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
