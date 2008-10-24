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
import net.link.safeonline.applet.AppletController;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;


/**
 * The identity applet creates an identity statement at the client-side within the browser and sends it over to a server-side servlet.
 * 
 * @author fcorneli
 * 
 */
public class IdentityApplet extends AppletBase {

    private static final long serialVersionUID = 1L;


    public IdentityApplet() {

        super();
    }

    public IdentityApplet(AppletController appletController) {

        super(appletController);
    }

    @Override
    public byte[] createStatement(Signer signer, IdentityProvider identityProvider) {

        String sessionId = getParameter("SessionId");
        String user = getParameter("User");
        String operation = getParameter("Operation");

        Locale locale = getLocale();
        ResourceBundle messages = ResourceBundle.getBundle("net.link.safeonline.identity.IdentityMessages", locale);

        super.outputInfoMessage(InfoLevel.NORMAL, messages.getString("creatingStmt"));
        super.outputDetailMessage("Session ID: " + sessionId);
        super.outputDetailMessage("User: " + user);
        super.outputDetailMessage("Operation: " + operation);
        byte[] identityStatement = IdentityStatementFactory.createIdentityStatement(sessionId, user, operation, signer, identityProvider);
        return identityStatement;
    }
}
