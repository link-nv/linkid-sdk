/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.util.LinkedList;
import java.util.List;

public class DatabasePluginManager {

	private static final List<DatabasePlugin> databasePlugins = new LinkedList<DatabasePlugin>();

	static {
		databasePlugins.add(new MySqlDatabasePlugin());
	}

	private DatabasePluginManager() {
		// empty
	}

	public static List<DatabasePlugin> initDatabasePlugins(
			ClassLoader classLoader) {
		List<DatabasePlugin> initializedDatabasePlugins = new LinkedList<DatabasePlugin>();
		for (DatabasePlugin databasePlugin : databasePlugins) {
			boolean result = databasePlugin.init(classLoader);
			if (true == result) {
				initializedDatabasePlugins.add(databasePlugin);
			}
		}
		return initializedDatabasePlugins;
	}
}
