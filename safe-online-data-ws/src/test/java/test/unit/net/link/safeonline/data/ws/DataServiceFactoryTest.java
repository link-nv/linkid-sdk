/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.data.ws;

import junit.framework.TestCase;
import liberty.dst._2006_08.ref.safe_online.DataService;
import net.link.safeonline.data.ws.DataServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataServiceFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(DataServiceFactoryTest.class);

	public void testNewInstance() throws Exception {
		// operate
		DataService result = DataServiceFactory.newInstance();

		// verify
		assertNotNull(result);
		LOG.debug("result service name: " + result.getServiceName());
	}
}
