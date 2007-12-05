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

import test.spike.net.link.safeonline.TinySyslogAppender.Facility;

public class SyslogTest {

	public static void main(String[] args) {
		Logger logger = Logger.getLogger(SyslogTest.class);

		TinySyslogAppender mySyslogAppender = new TinySyslogAppender(
				Facility.LOG_LOCAL0);
		logger.addAppender(mySyslogAppender);

		SyslogAppender oldSyslogAppender = new SyslogAppender();
		oldSyslogAppender.setSyslogHost("127.0.0.1");
		oldSyslogAppender.setFacility("LOCAL0");
		oldSyslogAppender.setThreshold(Level.DEBUG);
		oldSyslogAppender.setLayout(new SimpleLayout());
		logger.addAppender(oldSyslogAppender);

		logger.debug("hello world: " + new Date());
	}
}
