/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console.device;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.AppletControl;
import net.link.safeonline.applet.RuntimeContext;
import net.link.safeonline.sc.pkcs11.auth.AuthenticationApplet;
import net.link.safeonline.siemens.acceptance.ws.auth.console.AcceptanceConsole;
import net.link.safeonline.siemens.acceptance.ws.auth.console.DeviceAuthenticationPanel;


/**
 * BeId Authentication Panel.
 * 
 * @author wvdhaute
 * 
 */
public class BeIdAuthentication extends DeviceAuthenticationPanel {

    private static final long         serialVersionUID                  = 1L;

    private static final String       BEID_WS_AUTH_STATEMENT_ATTRIBUTE  = "urn:net:lin-k:safe-online:beid:ws:auth:statement";
    @SuppressWarnings("unused")
    private static final String       BEID_WS_AUTH_SESSION_ID_ATTRIBUTE = "urn:net:lin-k:safe-online:beid:ws:auth:sessionId";

    // used by BeIdAuthenticationServlet, cannot make servlet Observable ...
    private static BeIdAuthentication beIdAuthentication                = null;

    private JButton                   loginButton                       = new JButton("Login");
    private JButton                   cancelButton                      = new JButton("Cancel");

    private String                    sessionId                         = null;

    private String                    authenticationServletPath;


    public static BeIdAuthentication getInstance() {

        return beIdAuthentication;
    }

    public BeIdAuthentication(AcceptanceConsole parent) throws Exception {

        super(parent);

        buildWindow();
        handleEvents();
    }

    public BeIdAuthentication(AcceptanceConsole parent, DeviceAuthenticationInformationType deviceAuthenticationInformation)
                                                                                                                            throws Exception {

        super(parent, deviceAuthenticationInformation);

        sessionId = this.deviceAuthenticationInformation.getNameValuePair().get(0).getValue();

        // start jetty, statement sent to BeIdAuthenticationServlet
        authenticationServletPath = BeIdAuthenticationServletManager.getInstance().start();

        // set static for use in BeIdAuthenticationServlet
        beIdAuthentication = this;

        buildWindow();
        handleEvents();
    }

    private void buildWindow() {

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();

        if (null != sessionId) {
            final AuthenticationApplet authenticationApplet = getAuthenticationApplet();
            inputPanel.add(authenticationApplet, BorderLayout.CENTER);
            authenticationApplet.init();
            authenticationApplet.start();

        } else {
            inputPanel.add(new JLabel("Login first to retrieve session ID from OLAS", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        controlPanel.add(loginButton);
        controlPanel.add(cancelButton);
        if (!initial) {
            loginButton.setEnabled(false);
        }

        setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    private AuthenticationApplet getAuthenticationApplet() {

        AuthenticationApplet authenticationApplet;
        if (consoleManager.getUsePcscApplet()) {
            authenticationApplet = new net.link.safeonline.sc.pcsc.auth.AuthenticationApplet();
        } else {
            authenticationApplet = new AuthenticationApplet();
        }
        authenticationApplet.setIntegrated(true);
        authenticationApplet.setParameter(AuthenticationApplet.PARAM_SESSION_ID, sessionId);
        authenticationApplet.setParameter(AuthenticationApplet.PARAM_APPLICATION_ID, consoleManager.getApplication());
        authenticationApplet.setParameter(RuntimeContext.PARAM_SMARTCARD_CONFIG, "beid");
        authenticationApplet.setParameter(RuntimeContext.PARAM_SERVLET_PATH, authenticationServletPath);
        authenticationApplet.setParameter(AppletBase.PARAM_LANGUAGE, Locale.ENGLISH.getLanguage());
        authenticationApplet.setParameter(AppletBase.PARAM_BG_COLOR, "0xFFFFFF");
        authenticationApplet.setParameter(AppletControl.PARAM_NO_PCKS11_PATH, "foo");
        authenticationApplet.setParameter(AppletControl.PARAM_TARGET_PATH, null);
        return authenticationApplet;
    }

    private void handleEvents() {

        loginButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                authenticate();
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                cancel();
            }
        });
    }

    protected void authenticate() {

        authenticate(new HashMap<String, String>());

    }

    public void authenticate(String authenticationStatement) {

        BeIdAuthenticationServletManager.getInstance().shutDown();

        Map<String, String> deviceCredentials = new HashMap<String, String>();
        deviceCredentials.put(BEID_WS_AUTH_STATEMENT_ATTRIBUTE, authenticationStatement);
        authenticate(deviceCredentials);

    }
}
