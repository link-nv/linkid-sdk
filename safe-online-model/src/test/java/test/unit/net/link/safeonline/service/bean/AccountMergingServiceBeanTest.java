/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.data.AccountMergingDO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AccountMergingService;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.AccountMergingServiceBean;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class AccountMergingServiceBeanTest {

	static final Log LOG = LogFactory
			.getLog(AccountMergingServiceBeanTest.class);

	private EntityTestManager entityTestManager;

	private AttributeTypeService attributeTypeService;

	@Before
	public void setup() throws Exception {
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		systemStartable.postStart();

		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void testAccountMergingService() throws Exception {
		// setup
		String testTargetSubjectLogin = "test-target-subject-"
				+ UUID.randomUUID().toString();
		String testSourceSubjectLogin = "test-source-subject-"
				+ UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		// create accounts
		Account targetAccount = new Account(testTargetSubjectLogin,
				entityManager);
		Account sourceAccount = new Account(testSourceSubjectLogin,
				entityManager);

		// create attributes
		this.attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType1 = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:1", DatatypeType.STRING,
				true, false);
		AttributeTypeEntity attributeType2 = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:2", DatatypeType.STRING,
				true, false);
		AttributeTypeEntity attributeTypeMultivalued = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:multi",
				DatatypeType.STRING, true, true);
		AttributeTypeEntity attributeTypeCompounded = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:compound",
				DatatypeType.COMPOUNDED, true, true);
		AttributeTypeEntity attributeTypeCompoundMember1 = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:compound:member-1",
				DatatypeType.STRING, true, true);
		AttributeTypeEntity attributeTypeCompoundMember2 = addAttributeType(
				"urn::net:lin-k:safe-online:attribute:compound:member-2",
				DatatypeType.STRING, true, true);
		attributeTypeCompounded
				.addMember(attributeTypeCompoundMember1, 0, true);
		attributeTypeCompounded
				.addMember(attributeTypeCompoundMember2, 1, true);

		// create applications
		Application application1 = new Application("test-application-1", Arrays
				.asList(new AttributeTypeEntity[] { attributeType1,
						attributeType2 }), entityManager);
		Application application2 = new Application(
				"test-application-2",
				Arrays
						.asList(new AttributeTypeEntity[] { attributeTypeMultivalued }),
				entityManager);
		Application application3 = new Application("test-application-3", Arrays
				.asList(new AttributeTypeEntity[] { attributeTypeCompounded }),
				entityManager);
		Application application4 = new Application("test-application-4", Arrays
				.asList(new AttributeTypeEntity[] { attributeType1 }),
				entityManager);

		// subscribe
		targetAccount.addSubscription(application1);
		targetAccount.addSubscription(application2);
		targetAccount.addSubscription(application3);
		sourceAccount.addSubscription(application1);
		sourceAccount.addSubscription(application2);
		sourceAccount.addSubscription(application3);
		sourceAccount.addSubscription(application4);

		// operate
		AccountMergingService accountMergingService = EJBTestUtils.newInstance(
				AccountMergingServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				targetAccount.subject.getUserId(), SafeOnlineRoles.USER_ROLE);
		AccountMergingDO accountMergingDO = accountMergingService
				.getAccountMergingDO(sourceAccount.subjectLogin);

		// log
		accountMergingDO.log();

		// verify
		// keep in mind olas-user subscription and 3 password attributes ...
		assertEquals(4, accountMergingDO.getPreservedSubscriptions().size());
		assertEquals(1, accountMergingDO.getImportedSubscriptions().size());
		assertEquals(4, accountMergingDO.getPreservedAttributes().size());
		assertEquals(2, accountMergingDO.getChoosableAttributes().size());
		assertEquals(0, accountMergingDO.getImportedAttributes().size());
		assertEquals(8, accountMergingDO.getMergedAttributesToAdd().size());

		// operate
		accountMergingService.mergeAccount(accountMergingDO);

		// verify
		AttributeDAO attributeDAO = EJBTestUtils.newInstance(
				AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager, targetAccount.subject.getUserId(),
				SafeOnlineRoles.USER_ROLE);
		List<AttributeEntity> targetAttributesType1 = attributeDAO
				.listAttributes(targetAccount.subject, attributeType1);
		List<AttributeEntity> targetAttributesType2 = attributeDAO
				.listAttributes(targetAccount.subject, attributeType2);
		List<AttributeEntity> targetAttributesTypeMultivalued = attributeDAO
				.listAttributes(targetAccount.subject, attributeTypeMultivalued);
		List<AttributeEntity> targetAttributesTypeCompounded = attributeDAO
				.listAttributes(targetAccount.subject, attributeTypeCompounded);
		List<AttributeEntity> targetAttributesTypeCompoundMember1 = attributeDAO
				.listAttributes(targetAccount.subject,
						attributeTypeCompoundMember1);
		List<AttributeEntity> targetAttributesTypeCompoundMember2 = attributeDAO
				.listAttributes(targetAccount.subject,
						attributeTypeCompoundMember2);
		assertEquals(1, targetAttributesType1.size());
		assertEquals(1, targetAttributesType2.size());
		assertEquals(4, targetAttributesTypeMultivalued.size());
		assertEquals(4, targetAttributesTypeCompounded.size());
		assertEquals(4, targetAttributesTypeCompoundMember1.size());
		assertEquals(4, targetAttributesTypeCompoundMember2.size());
	}

	private class Account {
		String subjectLogin;

		SubjectEntity subject;

		List<String> subscriptions;

		List<AttributeDO> attributes;

		private EntityManager entityManager;

		public Account(String subjectLogin, EntityManager entityManager)
				throws Exception {
			this.subjectLogin = subjectLogin;
			this.entityManager = entityManager;
			UserRegistrationService userRegistrationService = EJBTestUtils
					.newInstance(UserRegistrationServiceBean.class,
							SafeOnlineTestContainer.sessionBeans,
							this.entityManager);
			userRegistrationService.registerUser(subjectLogin, "password");
			SubjectService subjectService = EJBTestUtils.newInstance(
					SubjectServiceBean.class,
					SafeOnlineTestContainer.sessionBeans, this.entityManager);
			this.subject = subjectService.findSubjectFromUserName(subjectLogin);
		}

		public void addSubscription(Application application) throws Exception {
			SubscriptionService subscriptionService = EJBTestUtils.newInstance(
					SubscriptionServiceBean.class,
					SafeOnlineTestContainer.sessionBeans, this.entityManager,
					this.subject.getUserId(), "user");
			subscriptionService.subscribe(application.applicationName);

			IdentityService identityService = EJBTestUtils.newInstance(
					IdentityServiceBean.class,
					SafeOnlineTestContainer.sessionBeans, this.entityManager,
					this.subject.getUserId(), "user");
			identityService.confirmIdentity(application.applicationName);

			for (AttributeTypeEntity attributeType : application.attributeTypes)
				addAttributeValue(attributeType, identityService);

		}

		private void addAttributeValue(AttributeTypeEntity attributeType,
				IdentityService identityService) throws Exception {
			LOG.debug("adding attribute value for: " + attributeType.getName()
					+ "(" + attributeType.getType() + ")");
			AttributeDO attribute = new AttributeDO(attributeType.getName(),
					attributeType.getType(), attributeType.isMultivalued(), 0,
					attributeType.getName(), null, attributeType
							.isUserEditable(), false, null, null);
			attribute.setMember(attributeType.isCompoundMember());
			if (DatatypeType.COMPOUNDED == attributeType.getType()) {
				attribute.setCompounded(true);
				identityService.saveAttribute(attribute);
				AttributeDO attribute2 = new AttributeDO(attributeType
						.getName(), attributeType.getType(), attributeType
						.isMultivalued(), 1, attributeType.getName(), null,
						attributeType.isUserEditable(), false, null, null);
				attribute2.setCompounded(true);
				identityService.saveAttribute(attribute2);
				for (CompoundedAttributeTypeMemberEntity member : attributeType
						.getMembers()) {
					LOG.debug("add compounded member value: "
							+ member.getMember().getName() + "("
							+ member.getMember().getType() + ")");
					addAttributeValue(member.getMember(), identityService);
				}
			} else {
				attribute.setStringValue(UUID.randomUUID().toString());
				identityService.saveAttribute(attribute);
				if (attributeType.isMultivalued()) {
					AttributeDO attribute2 = new AttributeDO(attributeType
							.getName(), attributeType.getType(), attributeType
							.isMultivalued(), 1, attributeType.getName(), null,
							attributeType.isUserEditable(), false, null, null);
					attribute2.setMember(attributeType.isCompoundMember());
					attribute2.setStringValue(UUID.randomUUID().toString());
					identityService.saveAttribute(attribute2);
				}
			}
		}
	}

	private class Application {
		String applicationName;

		List<AttributeTypeEntity> attributeTypes;

		private EntityManager entityManager;

		public Application(String applicationName,
				List<AttributeTypeEntity> attributeTypes,
				EntityManager entityManager) throws Exception {
			this.applicationName = applicationName;
			this.attributeTypes = attributeTypes;
			this.entityManager = entityManager;
			ApplicationService applicationService = EJBTestUtils.newInstance(
					ApplicationServiceBean.class,
					SafeOnlineTestContainer.sessionBeans, this.entityManager,
					"test-operator", "operator");
			List<IdentityAttributeTypeDO> applicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
			for (AttributeTypeEntity attributeType : this.attributeTypes)
				applicationIdentityAttributes.add(new IdentityAttributeTypeDO(
						attributeType.getName(), true, false));
			applicationService.addApplication(this.applicationName, null,
					"owner", null, false, IdScopeType.USER, null, null, null,
					null, applicationIdentityAttributes, false);
		}
	}

	private AttributeTypeEntity addAttributeType(String attributeName,
			DatatypeType datatypeType, boolean userEditable, boolean multivalued)
			throws Exception {
		AttributeTypeEntity attributeTypeEntity = new AttributeTypeEntity(
				attributeName, datatypeType, true, userEditable);
		attributeTypeEntity.setMultivalued(multivalued);
		this.attributeTypeService.add(attributeTypeEntity);
		return attributeTypeEntity;
	}

}
