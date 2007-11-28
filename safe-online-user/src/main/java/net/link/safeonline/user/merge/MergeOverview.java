/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.ChoosableAttributeDO;
import net.link.safeonline.service.SubscriptionDO;

@Local
public interface MergeOverview {

	/*
	 * Lifecycle
	 */
	void init();

	void destroyCallback();

	/*
	 * Factories
	 */
	List<String> provenDeviceListFactory();

	List<String> neededDeviceListFactory();

	List<SubscriptionEntity> preservedSubscriptionsListFactory();

	List<SubscriptionDO> importedSubscriptionsListFactory();

	List<AttributeDO> preservedAttributesListFactory();

	List<AttributeDO> importedAttributesListFactory();

	List<AttributeDO> mergedAttributesListFactory();

	List<ChoosableAttributeDO> choosableAttributesListFactory();

	/*
	 * Actions
	 */
	String commit();

}
