/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryObjectFactoryTest {

	private EntityTestManager entityTestManager;

	private EntityManager entityManager;

	@Before
	public void setUp() throws Exception {
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(MyTestEntity.class);
		this.entityManager = this.entityTestManager.getEntityManager();
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void createQueryObject() throws Exception {
		// operate
		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// verify
		assertNotNull(queryObject);
	}

	@Test
	public void simpleQueryWithEmptyResult() throws Exception {
		// setup
		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		List<MyTestEntity> result = queryObject.listAll();

		// verify
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void simpleQuery() throws Exception {
		// setup
		String testName = UUID.randomUUID().toString();
		MyTestEntity myTestEntity = new MyTestEntity(testName);
		this.entityManager.persist(myTestEntity);

		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		List<MyTestEntity> result = queryObject.listAll();

		// verify
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(testName, result.get(0).getName());
	}

	@Test
	public void queryWithParam() throws Exception {
		// setup
		String testName = UUID.randomUUID().toString();
		MyTestEntity myTestEntity = new MyTestEntity(testName);
		this.entityManager.persist(myTestEntity);

		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		List<MyTestEntity> result = queryObject.listAll(testName);

		// verify
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(testName, result.get(0).getName());

		// operate
		result = queryObject.listAll(testName + "foobar");

		// verify
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void singleResultQueryWithParam() throws Exception {
		// setup
		String testName = UUID.randomUUID().toString();
		MyTestEntity myTestEntity = new MyTestEntity(testName);
		this.entityManager.persist(myTestEntity);

		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		MyTestEntity result = queryObject.get(testName);

		// verify
		assertNotNull(result);
		assertEquals(testName, result.getName());

		// operate
		try {
			queryObject.get(testName + "foobar");
			fail();
		} catch (NoResultException e) {
			// expected
		}
	}

	@Test
	public void nullableSingleResultQueryWithParam() throws Exception {
		// setup
		String testName = UUID.randomUUID().toString();
		MyTestEntity myTestEntity = new MyTestEntity(testName);
		this.entityManager.persist(myTestEntity);

		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		MyTestEntity result = queryObject.get(testName);

		// verify
		assertNotNull(result);
		assertEquals(testName, result.getName());

		// operate
		result = queryObject.find(testName + "foobar");

		// verify
		assertNull(result);
	}

	@Test
	public void updateMethod() throws Exception {
		// setup
		String testName = UUID.randomUUID().toString();
		MyTestEntity myTestEntity = new MyTestEntity(testName);
		this.entityManager.persist(myTestEntity);

		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		queryObject.removeAll();
		queryObject.removeAllReturningInt();
		queryObject.removeAllReturningInteger();

		// operate
		MyTestEntity result = queryObject.find(testName);

		// verify
		assertNull(result);
	}

	@Test
	public void queryQueryMethod() throws Exception {
		// setup
		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		Query result = queryObject.listAllQuery();

		// verify
		assertNotNull(result);
	}

	@Test
	public void countQuery() throws Exception {
		// setup
		MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory
				.createQueryObject(this.entityManager,
						MyTestEntity.MyQueryTestInterface.class);

		// operate
		long count = queryObject.countAll();

		// verify
		assertTrue(0 == count);
	}
}
