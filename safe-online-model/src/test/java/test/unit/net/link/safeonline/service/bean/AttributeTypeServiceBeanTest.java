/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class AttributeTypeServiceBeanTest {

	private AttributeTypeService testedInstance;

	private EntityTestManager entityTestManager;

	@Before
	public void setUp() throws Exception {
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		this.testedInstance = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE,
				SafeOnlineRoles.OPERATOR_ROLE);
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void addAndList() throws Exception {
		// setup
		String attributeName = "test-attribute-type-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.STRING, true, true);

		// operate
		this.testedInstance.add(attributeType);

		List<AttributeTypeEntity> result = this.testedInstance
				.listAttributeTypes();

		// verify
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(attributeType, result.get(0));
	}

	@Test
	public void addCompoundedAndListAvailableMembers() throws Exception {
		// setup
		String memberAttributeName = "test-attribute-type-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				memberAttributeName, DatatypeType.STRING, true, true);

		String nonMemberAttributeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity nonMemberAttributeType = new AttributeTypeEntity(
				nonMemberAttributeName, DatatypeType.STRING, true, true);
		nonMemberAttributeType.setMultivalued(true);

		String compoundedAttributeTypeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(
				compoundedAttributeTypeName, DatatypeType.STRING, true, true);
		compoundedAttributeType.addMember(memberAttributeType, 0, true);

		// operate
		this.testedInstance.add(memberAttributeType);
		this.testedInstance.add(nonMemberAttributeType);

		this.testedInstance.add(compoundedAttributeType);

		List<AttributeTypeEntity> allResult = this.testedInstance
				.listAttributeTypes();

		List<AttributeTypeEntity> availableMemberResult = this.testedInstance
				.listAvailableMemberAttributeTypes();

		// verify
		assertNotNull(allResult);
		assertEquals(3, allResult.size());

		assertNotNull(availableMemberResult);
		assertEquals(1, availableMemberResult.size());
		assertEquals(nonMemberAttributeType, availableMemberResult.get(0));
	}

	@Test
	public void compoundedOfCompoundedNotAllowed() throws Exception {
		// setup
		String memberAttributeName = "test-attribute-type-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				memberAttributeName, DatatypeType.STRING, true, true);

		String compoundedAttributeTypeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(
				compoundedAttributeTypeName, DatatypeType.STRING, true, true);
		compoundedAttributeType.addMember(memberAttributeType, 0, true);

		String compoundedCompoundedAttributeTypeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity compoundedCompoundedAttributeType = new AttributeTypeEntity(
				compoundedCompoundedAttributeTypeName, DatatypeType.STRING,
				true, true);
		compoundedCompoundedAttributeType.addMember(compoundedAttributeType, 0,
				true);

		// operate
		this.testedInstance.add(memberAttributeType);
		this.testedInstance.add(compoundedAttributeType);

		try {
			this.testedInstance.add(compoundedCompoundedAttributeType);
			fail();
		} catch (AttributeTypeDefinitionException e) {
			// expected
		}
	}

	@Test
	public void compoundedOfExistingCompoundMemberNotAllowed() throws Exception {
		// setup
		String memberAttributeName = "test-attribute-type-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				memberAttributeName, DatatypeType.STRING, true, true);

		String compoundedAttributeTypeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(
				compoundedAttributeTypeName, DatatypeType.STRING, true, true);
		compoundedAttributeType.addMember(memberAttributeType, 0, true);

		String compoundedCompoundedAttributeTypeName = "test-attribute-type-name"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity compounded2AttributeType = new AttributeTypeEntity(
				compoundedCompoundedAttributeTypeName, DatatypeType.STRING,
				true, true);
		compounded2AttributeType.addMember(memberAttributeType, 0, true);

		// operate
		this.testedInstance.add(memberAttributeType);
		this.testedInstance.add(compoundedAttributeType);

		try {
			this.testedInstance.add(compounded2AttributeType);
			fail();
		} catch (AttributeTypeDefinitionException e) {
			// expected
		}
	}
}
