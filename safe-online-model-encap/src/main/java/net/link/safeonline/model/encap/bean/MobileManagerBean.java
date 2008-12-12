package net.link.safeonline.model.encap.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.model.encap.MobileManager;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClient;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClientImpl;
import net.link.safeonline.sdk.ws.encap.administration.EncapAdministrationClient;
import net.link.safeonline.sdk.ws.encap.administration.EncapAdministrationClientImpl;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClient;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClientImpl;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = MobileManager.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class MobileManagerBean implements MobileManager {

    @EJB(mappedName = ResourceAuditLogger.JNDI_BINDING)
    private ResourceAuditLogger resourceAuditLogger;

    @Configurable(name = "Encap Server", group = "Encap")
    private String              encapServerLocation     = "localhost:8080/safe-online-encap-ws";
    // private String encapServerLocation = "81.246.63.169:9090/mSec2";

    @Configurable(name = "Encap Organisation ID", group = "Encap")
    private String              encapOrganisationId     = "encap";
    // private String encapOrganisationId = "test1";

    @Configurable(name = "Encap Client Link", group = "Encap")
    private String              encapClientDownloadLink = "http://81.246.63.169:9090/mSec2/test/download.htm";


    /*
     * RemoteException are transformed to a MobileException, else they get wrapped by JBoss into EJBTransactionRolledbackException
     */
    public String requestOTP(String mobile)
            throws MobileException {

        try {
            EncapAuthenticationClient encapAuthenticationClient = new EncapAuthenticationClientImpl(this.encapServerLocation);
            return encapAuthenticationClient.challenge(mobile, this.encapOrganisationId);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public boolean verifyOTP(String challengeId, String OTPValue)
            throws MobileException {

        try {
            EncapAuthenticationClient encapAuthenticationClient = new EncapAuthenticationClientImpl(this.encapServerLocation);
            return encapAuthenticationClient.verifyOTP(challengeId, OTPValue);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public String activate(String mobile, String sessionInfo)
            throws MobileException {

        try {
            EncapActivationClient encapActivationClient = new EncapActivationClientImpl(this.encapServerLocation);
            return encapActivationClient.activate(mobile, this.encapOrganisationId, sessionInfo);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public void remove(String mobile)
            throws MobileException {

        try {
            EncapAdministrationClient encapAdministrationClient = new EncapAdministrationClientImpl(this.encapServerLocation);
            encapAdministrationClient.remove(mobile, this.encapOrganisationId);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public void lock(String mobile)
            throws MobileException {

        try {
            EncapAdministrationClient encapAdministrationClient = new EncapAdministrationClientImpl(this.encapServerLocation);
            encapAdministrationClient.lock(mobile, this.encapOrganisationId);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public void unLock(String mobile)
            throws MobileException {

        try {
            EncapAdministrationClient encapAdministrationClient = new EncapAdministrationClientImpl(this.encapServerLocation);
            encapAdministrationClient.unLock(mobile, this.encapOrganisationId);
        } catch (RemoteException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                    this.encapServerLocation, "Encap WS not available");
            throw new MobileException(e.getMessage());
        } catch (MalformedURLException e) {
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNKNOWN, this.encapServerLocation,
                    "Encap WS not valid");
            throw new RuntimeException(e);
        }
    }

    public String getClientDownloadLink() {

        return this.encapClientDownloadLink;
    }
}
