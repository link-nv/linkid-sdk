/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class CheckSchemaMenuAction extends AbstractMenuAction {

    public CheckSchemaMenuAction() {

        super('V', "Check Database Schema Version");
    }

    public void run() {

        System.out.println(super.getDescription());
        Connection connection = DatabasePluginManager.getConnection();
        try {
            Statement statement = connection.createStatement();
            try {
                ResultSet resultSet = statement.executeQuery("SELECT value FROM metadata WHERE name='"
                        + DatabaseConstants.METADATA_VERSION_NAME + "'");
                try {
                    if (resultSet.next()) {
                        String value = resultSet.getString(1);
                        System.out.println("Database schema version: " + value);
                        if (false == DatabaseConstants.VERSION.equals(value)) {
                            System.out.println("Database schema version should be: " + DatabaseConstants.VERSION);
                        } else {
                            System.out.println("Database schema version is OK.");
                        }
                    } else {
                        System.out.println("Database schema has no version in metadata table.");
                        System.out.println("Database has been incorrectly initialized.");
                    }
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            System.out.println("SQL error: " + e.getMessage());
            System.out.println("Database schema has no metadata table.");
            System.out.println("Database has been incorrectly initialized.");
        }
    }

    @Override
    public boolean isActive() {

        return DatabasePluginManager.hasActiveConnection();
    }
}
