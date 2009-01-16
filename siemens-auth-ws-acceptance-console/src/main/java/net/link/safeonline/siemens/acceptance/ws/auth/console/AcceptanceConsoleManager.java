/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.util.Observable;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.auth.AuthenticationClient;
import net.link.safeonline.sdk.ws.auth.AuthenticationClientImpl;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClient;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.w3c.dom.Document;


/**
 * 
 * Application console data class, observable for other registered observers
 * 
 * @author wvdhaute
 */
public class AcceptanceConsoleManager extends Observable {

    private static AcceptanceConsoleManager manager         = null;

    private String                          location        = "localhost";

    private String                          application     = "olas-user";

    private AuthenticationClient            authenticationClient;

    /*
     * For SOAP message viewing
     */
    private boolean                         captureMessages = true;
    private MessageAccessor                 messageAccessor = null;


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

    public void setMessageAccessor(MessageAccessor messageAccessor) {

        this.messageAccessor = messageAccessor;
        if (this.captureMessages) {
            setChanged();
            notifyObservers(messageAccessor);
        }
    }

    public void setCaptureMessages(boolean captureMessages) {

        this.captureMessages = captureMessages;
    }

    public Document getInboundMessage() {

        if (!this.captureMessages)
            return null;
        return this.messageAccessor.getInboundMessage();
    }

    public Document getOutboundMessage() {

        if (!this.captureMessages)
            return null;
        return this.messageAccessor.getOutboundMessage();
    }
}
