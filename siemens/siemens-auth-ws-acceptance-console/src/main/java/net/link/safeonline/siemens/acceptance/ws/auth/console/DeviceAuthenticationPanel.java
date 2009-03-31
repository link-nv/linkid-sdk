/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.util.Map;

import javax.swing.JPanel;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;


/**
 * <h2>{@link DeviceAuthenticationPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 20, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public abstract class DeviceAuthenticationPanel extends JPanel {

    private static final long                     serialVersionUID = 1L;

    protected DeviceAuthenticationInformationType deviceAuthenticationInformation;

    protected boolean                             initial          = true;

    protected AcceptanceConsole                   parent           = null;

    protected AcceptanceConsoleManager            consoleManager   = AcceptanceConsoleManager.getInstance();


    public DeviceAuthenticationPanel(AcceptanceConsole parent) {

        this.parent = parent;
        initial = true;
    }

    public DeviceAuthenticationPanel(AcceptanceConsole parent, DeviceAuthenticationInformationType deviceAuthenticationInformation) {

        this.deviceAuthenticationInformation = deviceAuthenticationInformation;
        this.parent = parent;
        initial = false;
    }

    public void authenticate(Map<String, String> deviceCredentials) {

        parent.login(deviceCredentials);
    }

    public void cancel() {

        parent.resetContent();
    }

}
