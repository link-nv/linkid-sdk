/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Observable;

import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.auth.AuthenticationClient;
import net.link.safeonline.sdk.ws.auth.AuthenticationClientImpl;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClient;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClient;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClientImpl;


/**
 * 
 * Application console data class, observable for other registered observers
 * 
 * @author wvdhaute
 */
public class AcceptanceConsoleManager extends Observable {

    private static AcceptanceConsoleManager manager         = null;

    private String                          location        = "https://localhost:8443/safe-online-auth-ws";

    private String                          application     = "olas-user";

    private boolean                         generateKeyPair = true;

    private KeyPair                         keyPair;

    private boolean                         usePcscApplet   = false;

    private AuthenticationClient            authenticationClient;

    private String                          userId;
    private String                          deviceName;


    public static AcceptanceConsoleManager getInstance() {

        if (null == manager) {
            manager = new AcceptanceConsoleManager();
        }
        return manager;
    }

    private AcceptanceConsoleManager() {

        // empty
    }

    public AuthenticationClient getAuthenticationClient()
            throws RequestDeniedException, WSClientTransportException {

        if (null == authenticationClient) {
            GetAuthenticationClient getAuthenticationClient = new GetAuthenticationClientImpl(location);
            authenticationClient = new AuthenticationClientImpl(getAuthenticationClient.getInstance());
        }
        return authenticationClient;
    }

    public void resetAuthenticationClient() {

        authenticationClient = null;
    }

    public SiemensAuthWsAcceptanceClient getAcceptanceWsClient() {

        if (null == authenticationClient || null == authenticationClient.getAssertion())
            return null;
        SiemensAuthWsAcceptanceClient client = new SiemensAuthWsAcceptanceClientImpl(location, authenticationClient.getAssertion());
        return client;
    }

    public void setLocation(String location) {

        this.location = location;
        setChanged();
        notifyObservers();
    }

    public String getLocation() {

        return location;
    }

    public void setApplication(String application) {

        this.application = application;
        setChanged();
        notifyObservers();
    }

    public String getApplication() {

        return application;
    }

    public boolean getGenerateKeyPair() {

        return generateKeyPair;
    }

    public void setGenerateKeyPair(boolean generateKeyPair) {

        this.generateKeyPair = generateKeyPair;
    }

    public PublicKey getPublicKey()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        if (generateKeyPair) {
            keyPair = KeyStoreUtils.generateKeyPair();
            return keyPair.getPublic();
        }
        return null;
    }

    public boolean getUsePcscApplet() {

        return usePcscApplet;
    }

    public void setUsePcscApplet(boolean usePcscApplet) {

        this.usePcscApplet = usePcscApplet;
    }

    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
    }

    public String getDeviceName() {

        return deviceName;
    }

    public void setUserId(String userId) {

        this.userId = userId;
        setChanged();
        notifyObservers();
    }

    public String getUserId() {

        return userId;
    }
}
