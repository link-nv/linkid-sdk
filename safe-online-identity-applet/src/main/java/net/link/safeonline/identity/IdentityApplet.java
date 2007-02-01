/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import net.link.safeonline.applet.AppletBase;
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
	protected byte[] createStatement(SmartCard smartCard) {
		outputInfoMessage(InfoLevel.NORMAL,
				"Creating the identity statement...");
		String user = getParameter("User");
		outputDetailMessage("User: " + user);
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, smartCard);
		return identityStatement;
	}
}
