/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline;

import net.link.safeonline.audit.bean.ResourceAuditLoggerBean;
import net.link.safeonline.audit.bean.SecurityAuditLoggerBean;
import net.link.safeonline.audit.dao.bean.AccessAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditContextDAOBean;
import net.link.safeonline.audit.dao.bean.ResourceAuditDAOBean;
import net.link.safeonline.audit.dao.bean.SecurityAuditDAOBean;
import net.link.safeonline.authentication.service.bean.AccountServiceBean;
import net.link.safeonline.authentication.service.bean.DevicePolicyServiceBean;
import net.link.safeonline.authentication.service.bean.ProxyAttributeServiceBean;
import net.link.safeonline.config.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.ApplicationPoolDAOBean;
import net.link.safeonline.dao.bean.ApplicationScopeIdDAOBean;
import net.link.safeonline.dao.bean.AttributeCacheDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.DeviceClassDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.HistoryDAOBean;
import net.link.safeonline.dao.bean.NodeDAOBean;
import net.link.safeonline.dao.bean.NodeMappingDAOBean;
import net.link.safeonline.dao.bean.StatisticDAOBean;
import net.link.safeonline.dao.bean.StatisticDataPointDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubjectIdentifierDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.UsageAgreementDAOBean;
import net.link.safeonline.device.backend.bean.CredentialManagerBean;
import net.link.safeonline.device.backend.bean.MobileManagerBean;
import net.link.safeonline.device.backend.bean.PasswordManagerBean;
import net.link.safeonline.device.bean.PasswordDeviceServiceBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.ApplicationScopeIdEntity;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryPropertyEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskContextDAOBean;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskEventDAOBean;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.ApplicationManagerBean;
import net.link.safeonline.model.bean.ApplicationOwnerManagerBean;
import net.link.safeonline.model.bean.ApplicationsBean;
import net.link.safeonline.model.bean.AttributeTypeDescriptionDecoratorBean;
import net.link.safeonline.model.bean.DevicesBean;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.bean.SubjectManagerBean;
import net.link.safeonline.model.bean.UsageAgreementManagerBean;
import net.link.safeonline.model.bean.UserRegistrationManagerBean;
import net.link.safeonline.notification.dao.bean.EndpointReferenceDAOBean;
import net.link.safeonline.notification.dao.bean.NotificationProducerDAOBean;
import net.link.safeonline.notification.service.bean.NotificationConsumerServiceBean;
import net.link.safeonline.notification.service.bean.NotificationProducerServiceBean;
import net.link.safeonline.osgi.bean.OSGIStartableBean;
import net.link.safeonline.pkix.dao.bean.CachedOcspResponseDAOBean;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.pkix.dao.bean.TrustPointDAOBean;
import net.link.safeonline.pkix.model.bean.CachedOcspValidatorBean;
import net.link.safeonline.pkix.model.bean.OcspValidatorBean;
import net.link.safeonline.pkix.model.bean.PkiProviderManagerBean;
import net.link.safeonline.pkix.model.bean.PkiValidatorBean;
import net.link.safeonline.service.bean.NodeMappingServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskHistoryDAOBean;


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


    public static final Class<?>[] sessionBeans = new Class[] { SubjectDAOBean.class, ApplicationDAOBean.class, SubscriptionDAOBean.class,
            AttributeDAOBean.class, TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
            ApplicationIdentityDAOBean.class, SubjectManagerBean.class, HistoryDAOBean.class, ApplicationOwnerManagerBean.class,
            ApplicationIdentityManagerBean.class, ConfigGroupDAOBean.class, ConfigItemDAOBean.class, TaskDAOBean.class,
            SchedulingDAOBean.class, TaskHistoryDAOBean.class, ApplicationManagerBean.class, TrustPointDAOBean.class,
            CachedOcspResponseDAOBean.class, AttributeTypeDescriptionDecoratorBean.class, AttributeProviderDAOBean.class,
            ApplicationsBean.class, DeviceDAOBean.class, DeviceClassDAOBean.class, AllowedDeviceDAOBean.class,
            HelpdeskContextDAOBean.class, HelpdeskEventDAOBean.class, CredentialManagerBean.class, PkiProviderManagerBean.class,
            PkiValidatorBean.class, CachedOcspValidatorBean.class, OcspValidatorBean.class, SubjectIdentifierDAOBean.class,
            UserRegistrationManagerBean.class, ResourceAuditLoggerBean.class, AuditAuditDAOBean.class, AuditContextDAOBean.class,
            AccessAuditDAOBean.class, SecurityAuditDAOBean.class, ResourceAuditDAOBean.class, PasswordManagerBean.class,
            SubjectServiceBean.class, IdGeneratorBean.class, UsageAgreementDAOBean.class, UsageAgreementManagerBean.class,
            PasswordDeviceServiceBean.class, MobileManagerBean.class, SecurityAuditLoggerBean.class, DevicePolicyServiceBean.class,
            DevicesBean.class, AccountServiceBean.class, NodeDAOBean.class, NodeMappingServiceBean.class, NodeMappingDAOBean.class,
            ProxyAttributeServiceBean.class, StatisticDAOBean.class, StatisticDataPointDAOBean.class,
            NotificationProducerServiceBean.class, NotificationProducerDAOBean.class, NotificationConsumerServiceBean.class,
            EndpointReferenceDAOBean.class, ApplicationScopeIdDAOBean.class, OSGIStartableBean.class, AttributeCacheDAOBean.class,
            ApplicationPoolDAOBean.class       };

    public static final Class<?>[] entities     = new Class[] { SubjectEntity.class, ApplicationEntity.class, ApplicationOwnerEntity.class,
            AttributeEntity.class, AttributeTypeEntity.class, SubscriptionEntity.class, TrustDomainEntity.class,
            ApplicationIdentityEntity.class, ConfigGroupEntity.class, ConfigItemEntity.class, TaskEntity.class, SchedulingEntity.class,
            TaskHistoryEntity.class, ApplicationIdentityAttributeEntity.class, TrustPointEntity.class,
            AttributeTypeDescriptionEntity.class, AttributeProviderEntity.class, DeviceEntity.class, AllowedDeviceEntity.class,
            DeviceClassEntity.class, DeviceDescriptionEntity.class, DeviceClassDescriptionEntity.class, DevicePropertyEntity.class,
            CompoundedAttributeTypeMemberEntity.class, HelpdeskContextEntity.class, HelpdeskEventEntity.class, HistoryEntity.class,
            HistoryPropertyEntity.class, SubjectIdentifierEntity.class, UsageAgreementEntity.class, UsageAgreementTextEntity.class,
            GlobalUsageAgreementEntity.class, NodeEntity.class, NodeMappingEntity.class, StatisticEntity.class,
            StatisticDataPointEntity.class, EndpointReferenceEntity.class, NotificationProducerSubscriptionEntity.class,
            ApplicationScopeIdEntity.class, AttributeCacheEntity.class, ApplicationPoolEntity.class };
}
