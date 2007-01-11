/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.identity;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.identity.IdentityApplet;

import junit.framework.TestCase;

public class IdentityAppletTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(IdentityAppletTest.class);

	public void testTransformUrl() throws Exception {
		// setup
		URL testDocumentBase = new URL(
				"http://buildserver:8080/safe-online/test.seam");

		// operate
		URL result = IdentityApplet.transformUrl(testDocumentBase, "identity/");

		// verify
		assertNotNull(result);
		LOG.debug("result: " + result);
		assertEquals("http://buildserver:8080/safe-online/identity/", result
				.toString());
	}
}
