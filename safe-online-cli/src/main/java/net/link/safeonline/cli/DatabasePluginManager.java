/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


public class DatabasePluginManager {

    private static final List<DatabasePlugin> databasePlugins = new LinkedList<DatabasePlugin>();

    private static Connection                 activeConnection;

    private static DatabasePlugin             activeDatabasePlugin;

    static {
        databasePlugins.add(new MySqlDatabasePlugin());
        databasePlugins.add(new PostgreSqlDatabasePlugin());
        databasePlugins.add(new OracleDatabasePlugin());
    }


    private DatabasePluginManager() {

        // empty
    }

    public static List<DatabasePlugin> initDatabasePlugins(ClassLoader classLoader) {

        List<DatabasePlugin> initializedDatabasePlugins = new LinkedList<DatabasePlugin>();
        for (DatabasePlugin databasePlugin : databasePlugins) {
            boolean result = databasePlugin.init(classLoader);
            if (true == result) {
                initializedDatabasePlugins.add(databasePlugin);
            }
        }
        return initializedDatabasePlugins;
    }

    public static Connection connect(DatabasePlugin databasePlugin, String connectionUrl, String user, String password) throws SQLException {

        if (null != activeConnection)
            throw new IllegalStateException("already an active connection");
        Connection connection = databasePlugin.getConnection(connectionUrl, user, password);
        activeConnection = connection;
        activeDatabasePlugin = databasePlugin;
        return connection;
    }

    public static boolean hasActiveConnection() {

        return null != activeConnection;
    }

    public static void disconnect() {

        if (null == activeConnection)
            throw new IllegalStateException("no active connection to close");
        try {
            activeConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException("error disconnecting: " + e.getMessage(), e);
        }
        activeConnection = null;
        activeDatabasePlugin = null;
    }

    public static Connection getConnection() {

        if (null == activeConnection)
            throw new IllegalStateException("no active connection");
        return activeConnection;
    }

    public static DatabasePlugin getDatabasePlugin() {

        if (null == activeDatabasePlugin)
            throw new IllegalStateException("no active database plugin");
        return activeDatabasePlugin;
    }
}
