/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.util.Locale;
import java.util.ResourceBundle;

import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.p11sc.SmartCard;

/**
 * The identity applet creates an identity statement at the client-side within
 * the browser.
 * 
 * @author fcorneli
 * 
 */
public class IdentityApplet extends AppletBase {

	private static final long serialVersionUID = 1L;

	@Override
	public byte[] createStatement(SmartCard smartCard) {

		Locale locale = getLocale();
		ResourceBundle messages = ResourceBundle.getBundle(
				"net.link.safeonline.identity.IdentityMessages", locale);

		outputInfoMessage(InfoLevel.NORMAL, messages.getString("creatingStmt"));
		String user = getParameter("User");
		outputDetailMessage("User: " + user);
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, smartCard);
		return identityStatement;
	}
}
