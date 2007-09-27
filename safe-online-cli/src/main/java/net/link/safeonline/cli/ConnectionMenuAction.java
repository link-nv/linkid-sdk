/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

public class ConnectionMenuAction extends AbstractMenuAction {

	public ConnectionMenuAction() {
		super('c', "Create Database Connection");
	}

	public void run() {
		System.out.println(super.getDescription());
		System.out.println();

		File jdbcDriverFile = getJDBCDriverFile();
		System.out.println("JDBC driver location: \""
				+ jdbcDriverFile.getAbsolutePath() + "\"");
		URL jdbcDriverUrl;
		try {
			jdbcDriverUrl = jdbcDriverFile.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL error: " + e.getMessage(), e);
		}

		Thread currentThread = Thread.currentThread();
		ClassLoader parentClassLoader = currentThread.getContextClassLoader();
		URLClassLoader classLoader = new URLClassLoader(
				new URL[] { jdbcDriverUrl }, parentClassLoader);
		currentThread.setContextClassLoader(classLoader);

		DatabasePlugin databasePlugin = initDatabasePlugin(classLoader);
		if (null == databasePlugin) {
			System.out.println("Cannot continue without database plugin.");
			return;
		}
		System.out.println("Selected database plugin: "
				+ databasePlugin.getName());

		Connection connection = connectToDatabase(databasePlugin);
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			System.out.println("Driver name: "
					+ databaseMetaData.getDriverName());
			System.out.println("Database product name: "
					+ databaseMetaData.getDatabaseProductName());
			System.out.println("Database product version: "
					+ databaseMetaData.getDatabaseProductVersion());
		} catch (SQLException e) {
			throw new RuntimeException("error retrieving database meta data: "
					+ e.getMessage());
		}
	}

	private Connection connectToDatabase(DatabasePlugin databasePlugin) {
		while (true) {
			System.out.print("Give the JDBC Connection URL: ");
			String connectionUrl = Keyboard.getString();
			System.out.print("Give the user: ");
			String user = Keyboard.getString();
			System.out.print("Give the password: ");
			String password = Keyboard.getString();
			try {
				Connection connection = databasePlugin.getConnection(
						connectionUrl, user, password);
				return connection;
			} catch (SQLException e) {
				System.err.println("Connection error: " + e.getMessage());
				e.printStackTrace();
				System.out.println("Try again.");
			}
		}
	}

	private DatabasePlugin initDatabasePlugin(ClassLoader classLoader) {
		List<DatabasePlugin> availableDatabasePlugins = DatabasePluginManager
				.initDatabasePlugins(classLoader);
		if (availableDatabasePlugins.isEmpty()) {
			System.out.println("No database plugins available.");
			return null;
		}
		System.out.println("Available database plugins:");
		int idx = 1;
		for (DatabasePlugin databasePlugin : availableDatabasePlugins) {
			System.out.println("[" + idx + "] " + databasePlugin.getName());
		}
		System.out.println("Please select a database plugin: ");
		int selectedIdx = Keyboard.getInteger() - 1;
		DatabasePlugin selectedDatabasePlugin = availableDatabasePlugins
				.get(selectedIdx);
		return selectedDatabasePlugin;
	}

	private File getJDBCDriverFile() {
		while (true) {
			System.out.print("Give the JDBC driver JAR location: ");
			String driverLocation = Keyboard.getString();
			File driverFile = new File(driverLocation);
			if (true == driverFile.exists()) {
				return driverFile;
			}
			System.out.println("File not found. Try again.");
		}
	}
}
