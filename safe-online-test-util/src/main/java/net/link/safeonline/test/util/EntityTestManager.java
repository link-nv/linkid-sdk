/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.Ejb3Configuration;

public class EntityTestManager {

	private static final Log LOG = LogFactory.getLog(EntityTestManager.class);

	private Ejb3Configuration configuration;

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

		this.configuration = new Ejb3Configuration();
		this.configuration.setProperty("hibernate.dialect",
				"org.hibernate.dialect.DerbyDialect");
		this.configuration.setProperty("hibernate.show_sql", "true");
		this.configuration.setProperty("hibernate.hbm2ddl.auto", "create");
		this.configuration.setProperty("hibernate.connection.driver_class",
				"org.apache.derby.jdbc.EmbeddedDriver");
		this.configuration.setProperty("hibernate.connection.url",
				"jdbc:derby:Test_Db;create=true");
		for (Class serializableClass : serializableClasses) {
			LOG.debug("adding annotated class: " + serializableClass.getName());
			this.configuration.addAnnotatedClass(serializableClass);
		}
		this.entityManagerFactory = this.configuration
				.createEntityManagerFactory();
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
				entityTransaction.commit();
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
}
