/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.Ejb3Configuration;

public class EntityTestManager {

	private static final Log LOG = LogFactory.getLog(EntityTestManager.class);

	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;

	@SuppressWarnings("deprecation")
	public void setUp(Class... serializableClasses) throws Exception {
		Ejb3Configuration configuration = new Ejb3Configuration();
		configuration.setProperty("hibernate.dialect",
				"org.hibernate.dialect.HSQLDialect");
		configuration.setProperty("hibernate.show_sql", "true");
		configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		configuration.setProperty("hibernate.connection.username", "sa");
		configuration.setProperty("hibernate.connection.password", "");
		configuration.setProperty("hibernate.connection.driver_class",
				"org.hsqldb.jdbcDriver");
		configuration.setProperty("hibernate.connection.url",
				"jdbc:hsqldb:mem:test");
		for (Class serializableClass : serializableClasses) {
			LOG.debug("adding annotated class: " + serializableClass.getName());
			configuration.addAnnotatedClass(serializableClass);
		}
		this.entityManagerFactory = configuration.createEntityManagerFactory();
		/*
		 * createEntityManagerFactory is deprecated, but
		 * buildEntityManagerFactory doesn't work because of a bug.
		 */
		this.entityManager = this.entityManagerFactory.createEntityManager();

		this.entityManager.getTransaction().begin();
	}

	public void tearDown() throws Exception {
		if (null == this.entityManager) {
			throw new IllegalStateException("invoke setUp first");
		}

		if (this.entityManager.isOpen()) {
			EntityTransaction entityTransaction = this.entityManager
					.getTransaction();
			if (entityTransaction.isActive()) {
				if (entityTransaction.getRollbackOnly()) {
					entityTransaction.rollback();
				} else {
					entityTransaction.commit();
				}
			}
			this.entityManager.close();
		}
		this.entityManagerFactory.close();
	}

	public EntityManager refreshEntityManager() {
		if (this.entityManager.isOpen()) {
			EntityTransaction entityTransaction = this.entityManager
					.getTransaction();
			if (entityTransaction.isActive()) {
				if (entityTransaction.getRollbackOnly()) {
					entityTransaction.rollback();
				} else {
					entityTransaction.commit();
				}
			}
			this.entityManager.close();
		}
		this.entityManager = this.entityManagerFactory.createEntityManager();
		this.entityManager.getTransaction().begin();
		return this.entityManager;
	}

	public void newTransaction() {
		LOG.debug("new transaction");
		EntityTransaction transaction = this.entityManager.getTransaction();
		transaction.commit();
		transaction.begin();
		this.entityManager.clear();
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	/**
	 * Create a new instance of the given class that has the test transaction
	 * entity manager handler applied to it. The transaction semantics are:
	 * 
	 * <code>@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)</code>
	 * 
	 * @param <Type>
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <Type> Type newInstance(Class<Type> clazz) {
		Type instance;
		try {
			instance = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("instantiation error");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("illegal access error");
		}
		TransactionMethodInterceptor transactionInvocationHandler = new TransactionMethodInterceptor(
				instance, this.entityManagerFactory);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(transactionInvocationHandler);
		Type object = (Type) enhancer.create();
		return object;
	}

	private static class TransactionMethodInterceptor implements
			MethodInterceptor {

		private final Object object;

		private final EntityManagerFactory entityManagerFactory;

		private final Field field;

		public TransactionMethodInterceptor(Object object,
				EntityManagerFactory entityManagerFactory) {
			this.object = object;
			this.entityManagerFactory = entityManagerFactory;
			this.field = getEntityManagerField(object);
		}

		private Field getEntityManagerField(Object object) {
			Class clazz = object.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				PersistenceContext persistenceContextAnnotation = field
						.getAnnotation(PersistenceContext.class);
				if (null == persistenceContextAnnotation) {
					continue;
				}
				if (false == EntityManager.class.isAssignableFrom(field
						.getType())) {
					throw new RuntimeException("field type not correct");
				}
				field.setAccessible(true);
				return field;
			}
			throw new RuntimeException("no entity manager field found");
		}

		private static final Log LOG = LogFactory
				.getLog(TransactionMethodInterceptor.class);

		public Object intercept(Object obj, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			EntityManager entityManager = this.entityManagerFactory
					.createEntityManager();
			try {
				this.field.set(this.object, entityManager);
				LOG.debug("begin transaction");
				entityManager.getTransaction().begin();
				Object result = method.invoke(this.object, args);
				LOG.debug("commit transaction");
				entityManager.getTransaction().commit();
				return result;
			} catch (InvocationTargetException e) {
				LOG.debug("rollback transaction");
				entityManager.getTransaction().rollback();
				throw e.getTargetException();
			} catch (Exception e) {
				LOG.error("exception received");
				throw e;
			} finally {
				entityManager.close();
			}
		}

	}
}
