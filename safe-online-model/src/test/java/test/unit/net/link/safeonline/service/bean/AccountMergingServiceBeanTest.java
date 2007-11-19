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
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AccountMergingDO;
import net.link.safeonline.service.AccountMergingService;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.AccountMergingServiceBean;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class AccountMergingServiceBeanTest {

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
				"test-attribute-1", DatatypeType.STRING, true, false);
		AttributeTypeEntity attributeType2 = addAttributeType(
				"test-attribute-2", DatatypeType.STRING, true, false);
		AttributeTypeEntity attributeTypeMultivalued = addAttributeType(
				"test-attribute-multi-1", DatatypeType.STRING, true, true);
		AttributeTypeEntity attributeTypeCompounded = addAttributeType(
				"test-attribute-compound-1", DatatypeType.COMPOUNDED, true,
				false);
		AttributeTypeEntity attributeTypeCompoundMember1 = addAttributeType(
				"test-attribute-compound-member-1", DatatypeType.STRING, true,
				false);
		AttributeTypeEntity attributeTypeCompoundMember2 = addAttributeType(
				"test-attribute-compound-member-2", DatatypeType.STRING, true,
				false);
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

		// subscribe
		targetAccount.addSubscription(application1);
		targetAccount.addSubscription(application2);
		sourceAccount.addSubscription(application1);
		sourceAccount.addSubscription(application2);
		sourceAccount.addSubscription(application3);

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
		assertEquals(3, accountMergingDO.getPreservedSubscriptions().size());
		assertEquals(1, accountMergingDO.getImportedSubscriptions().size());
		assertEquals(4, accountMergingDO.getPreservedAttributes().size());
		assertEquals(2, accountMergingDO.getChoosableAttributes().size());
		assertEquals(3, accountMergingDO.getImportedAttributes().size());
		assertEquals(4, accountMergingDO.getMergedAttributes().size());
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

			for (AttributeTypeEntity attributeType : application.attributeTypes) {
				addAttributeValue(attributeType, identityService);
			}

		}

		private void addAttributeValue(AttributeTypeEntity attributeType,
				IdentityService identityService) throws Exception {
			AttributeDO attribute = new AttributeDO(attributeType.getName(),
					attributeType.getType());
			attribute.setEditable(attributeType.isUserEditable());
			if (DatatypeType.COMPOUNDED == attributeType.getType()) {
				for (CompoundedAttributeTypeMemberEntity member : attributeType
						.getMembers()) {
					addAttributeValue(member.getMember(), identityService);
				}
			} else {
				attribute.setStringValue(attribute.getName() + "-value-"
						+ UUID.randomUUID().toString());
				identityService.saveAttribute(attribute);
				if (attributeType.isMultivalued()) {
					AttributeDO attribute2 = new AttributeDO(attributeType
							.getName(), attributeType.getType());
					attribute2.setEditable(attributeType.isUserEditable());
					attribute2.setStringValue(attribute2.getName() + "-value-"
							+ UUID.randomUUID().toString());
					attribute2.setIndex(1);
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
			for (AttributeTypeEntity attributeType : this.attributeTypes) {
				applicationIdentityAttributes.add(new IdentityAttributeTypeDO(
						attributeType.getName(), true, false));
			}
			applicationService.addApplication(this.applicationName, null,
					"owner", null, false, IdScopeType.USER, null, null,
					applicationIdentityAttributes);
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
