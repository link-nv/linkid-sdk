/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.seam;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.DeviceManager;

/**
 * Utility class for usage within a JBoss Seam JSF based web application.
 * 
 * @author wvdhaute
 * 
 */
public class SafeOnlineDeviceUtils {

	private SafeOnlineDeviceUtils() {
		// empty
	}

	/**
	 * Redirects from Device issuer to OLAS upon completion of the device
	 * registration/removal.
	 * 
	 * @throws IOException
	 */
	public static void deviceExit() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		externalContext
				.redirect(DeviceManager
						.getDeviceExitServiceUrl((HttpSession) externalContext
								.getSession(true)));
	}
}
