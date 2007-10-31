/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ping.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.ping.PingService;
import net.link.safeonline.ping.ws.PingServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * Unit test for ping service factory.
 * 
 * @author fcorneli
 */
public class PingServiceFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(PingServiceFactoryTest.class);

	@Test
	public void testNewInstance() throws Exception {
		// operate
		PingService result = PingServiceFactory.newInstance();

		// verify
		assertNotNull(result);
		QName serviceName = result.getServiceName();
		LOG.debug("service name: " + serviceName);
		assertEquals("PingService", serviceName.getLocalPart());
		assertEquals("urn:net:lin-k:safe-online:ping", serviceName
				.getNamespaceURI());
		LOG.debug("service name prefix: " + serviceName.getPrefix());
	}
}
