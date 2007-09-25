/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.net.SyslogAppender;
import org.junit.Test;

public class SyslogTest {

	@Test
	public void syslogViaLog4j() throws Exception {
		Logger logger = Logger.getLogger(SyslogTest.class);
		SyslogAppender syslogAppender = new SyslogAppender();
		syslogAppender.setSyslogHost("127.0.0.1");
		syslogAppender.setFacility("LOCAL0");
		syslogAppender.setThreshold(Level.DEBUG);
		syslogAppender.setLayout(new SimpleLayout());

		logger.addAppender(syslogAppender);
		logger.debug("hello world: " + new Date());
	}
}
