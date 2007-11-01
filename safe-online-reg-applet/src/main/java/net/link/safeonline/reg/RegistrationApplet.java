/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.reg;

import java.util.Locale;
import java.util.ResourceBundle;

import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.p11sc.SmartCard;

public class RegistrationApplet extends AppletBase {

	private static final long serialVersionUID = 1L;

	@Override
	public byte[] createStatement(SmartCard smartCard) {
		String sessionId = getParameter("SessionId");
		String applicationId = getParameter("ApplicationId");
		String user = getParameter("User");

		Locale locale = getLocale();
		ResourceBundle messages = ResourceBundle.getBundle(
				"net.link.safeonline.reg.RegistrationMessages", locale);

		outputInfoMessage(InfoLevel.NORMAL, messages.getString("creatingStmt"));
		outputDetailMessage("Session: " + sessionId);
		outputDetailMessage("Application: " + applicationId);
		outputDetailMessage("User: " + user);
		byte[] registrationStatement = RegistrationStatementFactory
				.createRegistrationStatement(user, sessionId, applicationId,
						smartCard);
		return registrationStatement;
	}
}
