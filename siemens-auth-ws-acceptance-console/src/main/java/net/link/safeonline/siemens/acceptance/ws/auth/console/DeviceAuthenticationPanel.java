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

    private String                                deviceName;

    protected DeviceAuthenticationInformationType deviceAuthenticationInformation;

    protected boolean                             initial          = true;

    protected AcceptanceConsole                   parent           = null;


    public DeviceAuthenticationPanel(String deviceName, AcceptanceConsole parent) {

        this.deviceName = deviceName;
        this.parent = parent;
        this.initial = true;
    }

    public DeviceAuthenticationPanel(String deviceName, AcceptanceConsole parent,
                                     DeviceAuthenticationInformationType deviceAuthenticationInformation) {

        this.deviceName = deviceName;
        this.deviceAuthenticationInformation = deviceAuthenticationInformation;
        this.parent = parent;
        this.initial = false;
    }

    public void authenticate(Map<String, String> deviceCredentials) {

        this.parent.login(this.deviceName, deviceCredentials);
    }

    public void cancel() {

        this.parent.resetContent();
    }

}
