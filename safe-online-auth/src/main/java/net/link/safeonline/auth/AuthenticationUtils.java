/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

	public static void redirectToApplication(String target, String username,
			FacesMessages facesMessages) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		LOG.debug("redirecting to:  " + target);
		String redirectUrl;
		try {
			redirectUrl = target + "?username="
					+ URLEncoder.encode(username, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "UnsupportedEncoding: " + e.getMessage();
			LOG.debug(msg);
			facesMessages.add(msg);
			return;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.debug(msg);
			facesMessages.add(msg);
			return;
		}
	}
}
