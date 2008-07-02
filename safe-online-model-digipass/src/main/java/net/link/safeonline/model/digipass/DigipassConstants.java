/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass;

import net.link.safeonline.Startable;

public class DigipassConstants {

	private DigipassConstants() {
		// empty
	}

	public static final String DIGIPASS_STARTABLE_JNDI_PREFIX = "SafeOnlineDigipass/startup/";
	
	public static final String DIGIPASS_DEVICE_ID = "digipass";

	public static final String DIGIPASS_IDENTIFIER_DOMAIN = "digipass";

	public static final String DIGIPASS_SN_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:digipass:serial-number";

	public static final int DIGIPASS_BOOT_PRIORITY = Startable.PRIORITY_BOOTSTRAP - 1;
}
