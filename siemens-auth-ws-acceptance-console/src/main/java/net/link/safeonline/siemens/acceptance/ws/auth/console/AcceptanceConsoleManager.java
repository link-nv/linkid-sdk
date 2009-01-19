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

    private AuthenticationClient            authenticationClient;


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

        if (null == this.authenticationClient) {
            GetAuthenticationClient getAuthenticationClient = new GetAuthenticationClientImpl(this.location);
            this.authenticationClient = new AuthenticationClientImpl(getAuthenticationClient.getInstance());
        }
        return this.authenticationClient;
    }

    public void resetAuthenticationClient() {

        this.authenticationClient = null;
    }

    public void setLocation(String location) {

        this.location = location;
        setChanged();
        notifyObservers();
    }

    public String getLocation() {

        return this.location;
    }

    public void setApplication(String application) {

        this.application = application;
        setChanged();
        notifyObservers();
    }

    public String getApplication() {

        return this.application;
    }

    public boolean getGenerateKeyPair() {

        return this.generateKeyPair;
    }

    public void setGenerateKeyPair(boolean generateKeyPair) {

        this.generateKeyPair = generateKeyPair;
    }

    public PublicKey getPublicKey()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        if (this.generateKeyPair) {
            this.keyPair = KeyStoreUtils.generateKeyPair();
            return this.keyPair.getPublic();
        }
        return null;
    }
}
