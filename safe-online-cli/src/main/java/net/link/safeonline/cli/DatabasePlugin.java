/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public interface DatabasePlugin {

    boolean init(ClassLoader classLoader);

    String getName();

    Connection getConnection(String connectionUrl, String user, String password) throws SQLException;

    List<String> getInitList();
}
