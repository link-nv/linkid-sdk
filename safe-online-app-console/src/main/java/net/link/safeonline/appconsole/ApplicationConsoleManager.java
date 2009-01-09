/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.IDENTITY;
import static net.link.safeonline.appconsole.Messages.LOCATION;

import java.io.File;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Observable;

import net.link.safeonline.sdk.ws.MessageAccessor;

import org.w3c.dom.Document;


/**
 * 
 * Application console data class, observable for other registered observers
 * 
 * @author wvdhaute
 */
public class ApplicationConsoleManager extends Observable {

    private static ApplicationConsoleManager manager             = null;

    private String                           identityLabelPrefix = IDENTITY.getMessage() + " : ";
    private String                           locationLabelPrefix = LOCATION.getMessage() + " : ";

    private String                           identityLabel       = null;
    private String                           locationLabel       = null;
    private String                           location            = "localhost";

    private String                           keyStorePath        = null;
    private String                           keyStoreType        = null;
    private String                           keyStorePassword    = null;
    private PrivateKeyEntry                  identity            = null;

    /*
     * For SOAP message viewing
     */
    private boolean                          captureMessages     = true;
    private MessageAccessor                  messageAccessor     = null;


    public static ApplicationConsoleManager getInstance() {

        if (null == manager) {
            manager = new ApplicationConsoleManager();
        }
        return manager;
    }

    /*
     * Constructor ( singleton )
     */
    private ApplicationConsoleManager() {

        identityLabel = identityLabelPrefix;
        locationLabel = locationLabelPrefix + location;
    }

    public String getIdentityLabel() {

        return identityLabel;
    }

    public String getLocationLabel() {

        return locationLabel;
    }

    public String getLocation() {

        return location;
    }

    public void setLocation(String location) {

        this.location = location;
        locationLabel = locationLabelPrefix + location;
        setChanged();
        notifyObservers();

    }

    public PrivateKeyEntry getIdentity() {

        return identity;
    }

    public void setIdentity(PrivateKeyEntry identity, String keyStorePath, String keyStoreType, String keyStorePassword) {

        if (null == identity) {
            this.identity = null;
            this.keyStorePath = null;
            this.keyStoreType = null;
            this.keyStorePassword = null;
            identityLabel = identityLabelPrefix;
        } else {
            this.identity = identity;
            this.keyStorePath = keyStorePath;
            this.keyStoreType = keyStoreType;
            this.keyStorePassword = keyStorePassword;
            identityLabel = identityLabelPrefix
                    + ((X509Certificate) identity.getCertificate()).getSubjectX500Principal().getName() + " ( "
                    + new File(this.keyStorePath).getName() + " )";
        }
        setChanged();
        notifyObservers();
    }

    public void setMessageAccessor(MessageAccessor messageAccessor) {

        this.messageAccessor = messageAccessor;
        if (captureMessages) {
            setChanged();
            notifyObservers(messageAccessor);
        }
    }

    public void setCaptureMessages(boolean captureMessages) {

        this.captureMessages = captureMessages;
    }

    public Document getInboundMessage() {

        if (!captureMessages)
            return null;
        return messageAccessor.getInboundMessage();
    }

    public Document getOutboundMessage() {

        if (!captureMessages)
            return null;
        return messageAccessor.getOutboundMessage();
    }

    public String getKeyStorePath() {

        return keyStorePath;
    }

    public String getKeyStoreType() {

        return keyStoreType;
    }

    public String getKeyStorePassword() {

        return keyStorePassword;
    }

}
