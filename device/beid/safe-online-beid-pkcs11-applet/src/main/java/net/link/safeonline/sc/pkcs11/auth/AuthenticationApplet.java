/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pkcs11.auth;

import java.util.Locale;
import java.util.ResourceBundle;

import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.AppletController;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;


public class AuthenticationApplet extends AppletBase {

    private static final long  serialVersionUID     = 1L;

    public static final String PARAM_SESSION_ID     = "sessionId";
    public static final String PARAM_APPLICATION_ID = "applicationId";


    public AuthenticationApplet() {

        super();
    }

    protected AuthenticationApplet(AppletController appletController) {

        super(appletController);
    }

    @Override
    public byte[] createStatement(Signer signer, IdentityProvider identityProvider) {

        String sessionId = getParameter(PARAM_SESSION_ID);
        String applicationId = getParameter(PARAM_APPLICATION_ID);

        Locale locale = getLocale();
        ResourceBundle messages = ResourceBundle.getBundle("net.link.safeonline.sc.pkcs11.auth.AuthenticationMessages", locale);

        outputInfoMessage(InfoLevel.NORMAL, messages.getString("creatingStmt"));
        outputDetailMessage("Session: " + sessionId);
        outputDetailMessage("Application: " + applicationId);
        try {
            byte[] authenticationStatement = AuthenticationStatementFactory.createAuthenticationStatement(sessionId, applicationId, signer);
            return authenticationStatement;
        } catch (RuntimeException e) {
            outputDetailMessage("runtime exception: " + e.getMessage());
            throw e;
        }
    }
}
