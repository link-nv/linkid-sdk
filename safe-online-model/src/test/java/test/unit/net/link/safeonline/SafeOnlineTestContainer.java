/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline;

import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.CachedOcspResponseDAOBean;
import net.link.safeonline.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.bean.HistoryDAOBean;
import net.link.safeonline.dao.bean.SchedulingDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.TaskDAOBean;
import net.link.safeonline.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.dao.bean.TrustPointDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.ApplicationManagerBean;
import net.link.safeonline.model.bean.ApplicationOwnerManagerBean;
import net.link.safeonline.model.bean.AttributeTypeDescriptionDecoratorBean;
import net.link.safeonline.model.bean.SubjectManagerBean;

/**
 * Represents the content of the SafeOnline container.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineTestContainer {

	private SafeOnlineTestContainer() {
		// empty
	}

	public static final Class[] sessionBeans = new Class[] {
			SubjectDAOBean.class, ApplicationDAOBean.class,
			SubscriptionDAOBean.class, AttributeDAOBean.class,
			TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class,
			AttributeTypeDAOBean.class, ApplicationIdentityDAOBean.class,
			SubjectManagerBean.class, HistoryDAOBean.class,
			ApplicationOwnerManagerBean.class,
			ApplicationIdentityManagerBean.class, ConfigGroupDAOBean.class,
			ConfigItemDAOBean.class, TaskDAOBean.class,
			SchedulingDAOBean.class, TaskHistoryDAOBean.class,
			ApplicationManagerBean.class, TrustPointDAOBean.class,
			CachedOcspResponseDAOBean.class,
			AttributeTypeDescriptionDecoratorBean.class,
			AttributeProviderDAOBean.class };

	public static final Class[] entities = new Class[] { SubjectEntity.class,
			ApplicationEntity.class, ApplicationOwnerEntity.class,
			AttributeEntity.class, AttributeTypeEntity.class,
			SubscriptionEntity.class, TrustDomainEntity.class,
			ApplicationIdentityEntity.class, ConfigGroupEntity.class,
			ConfigItemEntity.class, TaskEntity.class, SchedulingEntity.class,
			TaskHistoryEntity.class, ApplicationIdentityAttributeEntity.class,
			TrustPointEntity.class, AttributeTypeDescriptionEntity.class };
}
