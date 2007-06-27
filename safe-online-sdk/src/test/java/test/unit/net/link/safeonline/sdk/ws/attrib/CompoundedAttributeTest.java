/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws.attrib;

import org.junit.Test;

import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;

public class CompoundedAttributeTest {

	@Compound("test-compound-attribute-name")
	public static class MyTestCompoundedAttribute {

		private String name;

		@CompoundMember("test-member-attribute-name")
		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Test
	public void instance() throws Exception {
		new MyTestCompoundedAttribute();
	}
}
