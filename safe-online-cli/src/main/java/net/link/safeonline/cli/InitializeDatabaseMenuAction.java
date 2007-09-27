/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class InitializeDatabaseMenuAction extends AbstractMenuAction {

	public InitializeDatabaseMenuAction() {
		super('i', "Initialize Database Schema");
	}

	public void run() {
		System.out.println(super.getDescription());
		DatabasePlugin databasePlugin = DatabasePluginManager
				.getDatabasePlugin();
		List<String> initList = databasePlugin.getInitList();
		Connection connection = DatabasePluginManager.getConnection();
		try {
			for (String initCmd : initList) {
				Statement statement = connection.createStatement();
				try {
					System.out.println("Executing: " + initCmd);
					statement.executeUpdate(initCmd);
				} finally {
					statement.close();
				}
			}
			Statement statement = connection.createStatement();
			try {
				statement
						.executeUpdate("INSERT INTO metadata(name, value) VALUES ('"
								+ DatabaseConstants.METADATA_VERSION_NAME
								+ "','" + DatabaseConstants.VERSION + "')");
			} finally {
				statement.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException("SQL error: " + e.getMessage(), e);
		}
		System.out.println("Database correctly initialized.");
	}

	@Override
	public boolean isActive() {
		return DatabasePluginManager.hasActiveConnection();
	}
}
