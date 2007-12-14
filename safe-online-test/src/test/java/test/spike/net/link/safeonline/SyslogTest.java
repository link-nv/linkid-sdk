/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.util.Date;

import net.link.safeonline.audit.TinySyslogger;
import net.link.safeonline.audit.TinySyslogger.Facility;

import org.apache.log4j.Logger;

public class SyslogTest {

	public static void main(String[] args) {
		Logger logger = Logger.getLogger(SyslogTest.class);

		TinySyslogger mySyslogAppender = new TinySyslogger(Facility.LOCAL0);

		mySyslogAppender.log("hello world: " + new Date());
	}
}
