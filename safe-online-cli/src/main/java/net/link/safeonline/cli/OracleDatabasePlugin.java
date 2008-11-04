/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


public class OracleDatabasePlugin implements DatabasePlugin {

    public static final String DRIVER_CLASS_NAME     = "oracle.jdbc.driver.OracleDriver";

    public static final String SQL_DDL_RESOURCE_NAME = "oracle-safe-online-ddl.sql";

    private Driver             driver;


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

        return "Oracle";
    }

    public Connection getConnection(String connectionUrl, String user, String password)
            throws SQLException {

        if (null == this.driver)
            throw new SQLException("need to initialize the database plugin first");
        Properties info = new Properties();
        info.put("user", user);
        info.put("password", password);
        Connection connection = this.driver.connect(connectionUrl, info);
        return connection;
    }

    public List<String> getInitList() {

        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SQL_DDL_RESOURCE_NAME);
        if (null == inputStream)
            throw new RuntimeException(SQL_DDL_RESOURCE_NAME + " not found");

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        StringTokenizer tokenizer = new StringTokenizer(buffer.toString(), ";", false);
        List<String> initList = new LinkedList<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            initList.add(token);
        }

        return initList;
    }
}
