/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.Locale;
import java.util.ResourceBundle;

import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.p11sc.SmartCard;

public class AuthenticationApplet extends AppletBase {

	private static final long serialVersionUID = 1L;

	@Override
	public byte[] createStatement(SmartCard smartCard) {
		String sessionId = getParameter("SessionId");
		String applicationId = getParameter("ApplicationId");

		Locale locale = getLocale();
		ResourceBundle messages = ResourceBundle.getBundle(
				"net.link.safeonline.auth.AuthenticationMessages", locale);

		outputInfoMessage(InfoLevel.NORMAL, messages.getString("creatingStmt"));
		outputDetailMessage("Session: " + sessionId);
		outputDetailMessage("Application: " + applicationId);
		byte[] authenticationStatement = AuthenticationStatementFactory
				.createAuthenticationStatement(sessionId, applicationId,
						smartCard);
		return authenticationStatement;
	}
}
