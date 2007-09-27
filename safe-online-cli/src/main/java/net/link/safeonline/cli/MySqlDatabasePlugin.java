/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlDatabasePlugin implements DatabasePlugin {

	public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	private Driver driver;

	public boolean init(ClassLoader classLoader) {
		try {
			Class<?> driverClass = classLoader.loadClass(DRIVER_CLASS_NAME);
			Object driverInstance = driverClass.newInstance();
			if (driverInstance instanceof Driver) {
				this.driver = (Driver) driverInstance;
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public String getName() {
		return "MySQL";
	}

	public Connection getConnection(String connectionUrl, String user,
			String password) throws SQLException {
		if (null == this.driver) {
			throw new SQLException(
					"need to initialize the database plugin first");
		}
		Properties info = new Properties();
		info.put("user", user);
		info.put("password", password);
		Connection connection = this.driver.connect(connectionUrl, info);
		return connection;
	}
}
