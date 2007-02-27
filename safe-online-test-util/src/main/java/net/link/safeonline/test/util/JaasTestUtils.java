/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class JaasTestUtils {

	private JaasTestUtils() {
		// empty
	}

	public static void initJaasLoginModule(Class clazz) throws IOException {
		File jaasConfigFile = File.createTempFile("jaas-", ".login");
		PrintWriter printWriter = new PrintWriter(jaasConfigFile);
		printWriter.println("client-login {");
		printWriter.println(clazz.getName() + " required debug=true;");
		printWriter.println("};");
		printWriter.close();
		System.setProperty("java.security.auth.login.config", jaasConfigFile
				.getAbsolutePath());
	}
}
