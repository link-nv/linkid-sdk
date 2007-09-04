/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.core.FacesMessages;

public class AuthenticationUtils {

	private static final Log LOG = LogFactory.getLog(AuthenticationUtils.class);

	private AuthenticationUtils() {
		// empty
	}

	public static void commitAuthentication(FacesMessages facesMessages) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect("./exit");
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.debug(msg);
			facesMessages.add(msg);
			return;
		}
	}
}
