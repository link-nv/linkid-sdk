/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.Ejb3Configuration;

public class EntityTestManager {

	private static final Log LOG = LogFactory.getLog(EntityTestManager.class);

	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;

	private static final class DatabaseShutdownHook extends Thread {

		private static final Log LOG = LogFactory
				.getLog(DatabaseShutdownHook.class);

		private File tmpDbDir;

		public DatabaseShutdownHook(File tmpDbDir) {
			this.tmpDbDir = tmpDbDir;
		}

		@Override
		public void run() {
			LOG.debug("run database shutdown hook");
			try {
				FileUtils.deleteDirectory(this.tmpDbDir);
			} catch (IOException e) {
				LOG.error("could not delete directory: " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void setUp(Class... serializableClasses) throws Exception {
		File tmpDbDir = File.createTempFile("derby-", "-db");
		LOG.debug("tmp db dir: " + tmpDbDir);
		if (!tmpDbDir.delete()) {
			throw new RuntimeException("Could not delete "
					+ tmpDbDir.getAbsolutePath());
		}
		if (!tmpDbDir.mkdirs()) {
			throw new RuntimeException("Could not mkdirs: "
					+ tmpDbDir.getAbsolutePath());
		}
		if (!tmpDbDir.isDirectory()) {
			throw new RuntimeException("Is not a directory: "
					+ tmpDbDir.getAbsolutePath());
		}
		System.setProperty("derby.system.home", tmpDbDir.getAbsolutePath());
		Runtime.getRuntime()
				.addShutdownHook(new DatabaseShutdownHook(tmpDbDir));

		Ejb3Configuration configuration = new Ejb3Configuration();
		configuration.setProperty("hibernate.dialect",
				"org.hibernate.dialect.DerbyDialect");
		configuration.setProperty("hibernate.show_sql", "true");
		configuration.setProperty("hibernate.hbm2ddl.auto", "create");
		configuration.setProperty("hibernate.connection.driver_class",
				"org.apache.derby.jdbc.EmbeddedDriver");
		String dbName = "Test_DB_" + System.currentTimeMillis();
		configuration.setProperty("hibernate.connection.url", "jdbc:derby:"
				+ dbName + ";create=true");
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
		InvocationHandler transactionInvocationHandler = new TransactionInvocationHandler(
				instance, this.entityManagerFactory);
		Type proxy = (Type) Proxy.newProxyInstance(clazz.getClassLoader(),
				clazz.getInterfaces(), transactionInvocationHandler);
		return proxy;
	}

	private static class TransactionInvocationHandler implements
			InvocationHandler {

		private final Object object;

		private final EntityManagerFactory entityManagerFactory;

		private final Field field;

		public TransactionInvocationHandler(Object object,
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
				.getLog(TransactionInvocationHandler.class);

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
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
