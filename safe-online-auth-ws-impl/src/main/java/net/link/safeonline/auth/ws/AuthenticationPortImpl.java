/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesSaveRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementResponseType;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.ws.client.DeviceAuthenticationClient;
import net.link.safeonline.auth.ws.client.DeviceAuthenticationClientImpl;
import net.link.safeonline.auth.ws.client.GetDeviceAuthenticationClient;
import net.link.safeonline.auth.ws.client.GetDeviceAuthenticationClientImpl;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.saml.common.Saml2Util;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.auth.DataType;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.Injection;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.StatementAbstractType;
import oasis.names.tc.saml._2_0.assertion.SubjectConfirmationType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful Authentication web service using JAX-WS.
 * 
 * 
 * Do NOT use {@link Injection} as this is a {@link Stateful} web service and the statefulness is achieved by JAX-WS using the same
 * {@link InstanceResolver} as is used by the {@link Injection}.
 * 
 * @author wvdhaute
 * 
 */

@Stateful
@Addressing
@WebService(endpointInterface = "net.lin_k.safe_online.auth.AuthenticationPort")
@HandlerChain(file = "ws-auth-handlers.xml")
public class AuthenticationPortImpl implements AuthenticationPort {

    private static final Log                                    LOG                        = LogFactory
                                                                                                       .getLog(AuthenticationPortImpl.class);

    public static StatefulWebServiceManager<AuthenticationPort> manager;

    private DeviceAuthenticationClient                          deviceAuthenticationClient;

    private SubjectEntity                                       authenticatedSubject;

    private DeviceEntity                                        authenticatedDevice;

    private ApplicationEntity                                   application;

    private String                                              applicationName;

    private String                                              language;

    private KeyInfoType                                         keyInfo;

    private boolean                                             globalConfirmationRequired = true;
    private boolean                                             subscriptionRequired       = true;
    private boolean                                             confirmationRequired       = true;
    private boolean                                             hasMissingAttributes       = true;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    public AuthenticationPortImpl() {

        if (null != manager) {
            WSAuthenticationService wsAuthenticationService = EjbUtils.getEJB(WSAuthenticationService.JNDI_BINDING,
                    WSAuthenticationService.class);
            manager.setTimeout(1000 * wsAuthenticationService.getAuthenticationTimeout(), new TimeoutCallback());
        }
    }

    public AuthenticationPortImpl(DeviceAuthenticationClient deviceAuthenticationClient) {

        this();
        this.deviceAuthenticationClient = deviceAuthenticationClient;
    }


    class TimeoutCallback implements StatefulWebServiceManager.Callback<AuthenticationPort> {

        /**
         * {@inheritDoc}
         */
        public void onTimeout(AuthenticationPort timedOutObject, StatefulWebServiceManager<AuthenticationPort> serviceManager) {

        }

    }


    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request) {

        LOG.debug("authenticate");

        if (null != authenticatedSubject) {
            LOG.error("already authenticated user " + authenticatedSubject.getUserId());
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.ALREADY_AUTHENTICATED, null);
        }

        language = request.getLanguage();
        keyInfo = request.getKeyInfo();
        applicationName = request.getApplicationId();

        // proxy request to specified device
        WSAuthenticationResponseType response;
        try {
            response = proxyRequest(request);
        } catch (RequestDeniedException e) {
            LOG.error("request denied: " + e.getMessage(), e);
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.REQUEST_DENIED, e.getMessage());
        } catch (WSClientTransportException e) {
            LOG.error("request failed: " + e.getMessage(), e);
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.REQUEST_FAILED, e.getMessage());
        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), e.getErrorCode(), e.getMessage());
        }

        try {
            authenticatedSubject = findSubject(response);
            if (null == authenticatedSubject) {
                // not yet authenticated, device authentication ws is returning extra information for authentication
                LOG.debug("not yet authenticated, forward response");
                return response;
            }
            authenticatedDevice = getAuthenticatedDevice(response);

            LOG.debug("authenticated: userId=" + authenticatedSubject.getUserId());

            login(request.getID(), response);

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            setStatus(response, e.getErrorCode(), e.getMessage());
            manager.unexport(this);
            return response;

        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationGlobalUsageAgreementResponseType requestGlobalUsageAgreement(
                                                                                        WSAuthenticationGlobalUsageAgreementRequestType request) {

        LOG.debug("request global usage agreement");

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createGlobalUsageAgreementResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        WSAuthenticationGlobalUsageAgreementResponseType response = createGlobalUsageAgreementResponse(request.getID(),
                WSAuthenticationErrorCode.SUCCESS, null);

        try {
            if (!globalConfirmationRequired) {
                login(request.getID(), response);
                return response;
            }

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {

                // lookup global usage agreement
                UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING,
                        UsageAgreementService.class);
                String globalUsageAgreement = usageAgreementService.getGlobalUsageAgreementText(language);

                response.setGlobalUsageAgreement(globalUsageAgreement);

            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            manager.unexport(this);
            return createGlobalUsageAgreementResponse(request.getID(), e.getErrorCode(), e.getMessage());
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType confirmGlobalUsageAgreement(WSAuthenticationGlobalUsageAgreementConfirmationType request) {

        LOG.debug("confirm global usage agreement");

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        Confirmation confirmation = Confirmation.getConfirmation(request.getConfirmation());
        if (Confirmation.REJECT == confirmation)
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.AUTHENTICATION_FAILED, null);

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {

                // confirm global usage agreement
                UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING,
                        UsageAgreementService.class);
                usageAgreementService.confirmGlobalUsageAgreementVersion();
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

            login(request.getID(), response);

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            setStatus(response, e.getErrorCode(), e.getMessage());
            manager.unexport(this);
            return response;
        }

        return response;

    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationUsageAgreementResponseType requestUsageAgreement(WSAuthenticationUsageAgreementRequestType request) {

        LOG.debug("request application usage agreement / subscription : " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createUsageAgreementResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        WSAuthenticationUsageAgreementResponseType response = createUsageAgreementResponse(request.getID(),
                WSAuthenticationErrorCode.SUCCESS, null);

        try {
            if (!subscriptionRequired) {
                login(request.getID(), response);
                return response;
            }

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                getApplication();

                // lookup application's usage agreement
                UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING,
                        UsageAgreementService.class);
                String usageAgreement = usageAgreementService.getUsageAgreementText(application.getId(), language);
                if (null == usageAgreement) {
                    // check is subscription is needed
                    SubscriptionService subscriptionService = EjbUtils.getEJB(SubscriptionService.JNDI_BINDING, SubscriptionService.class);
                    if (!subscriptionService.isSubscribed(application.getId())) {
                        usageAgreement = "";
                    }
                }

                response.setUsageAgreement(usageAgreement);

            } catch (ApplicationNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            manager.unexport(this);
            return createUsageAgreementResponse(request.getID(), e.getErrorCode(), e.getMessage());
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType confirmUsageAgreement(WSAuthenticationUsageAgreementConfirmationType request) {

        LOG.debug("confirm application usage agreement / subscribe : " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        Confirmation confirmation = Confirmation.getConfirmation(request.getConfirmation());
        if (Confirmation.REJECT == confirmation)
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.AUTHENTICATION_FAILED, null);

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                getApplication();

                // confirm application usage agreement / subscribe
                UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING,
                        UsageAgreementService.class);
                SubscriptionService subscriptionService = EjbUtils.getEJB(SubscriptionService.JNDI_BINDING, SubscriptionService.class);

                if (!subscriptionService.isSubscribed(application.getId())) {
                    LOG.debug("subscribe to application " + application.getName());
                    subscriptionService.subscribe(application.getId());
                }

                if (usageAgreementService.requiresUsageAgreementAcceptation(application.getId(), language)) {
                    LOG.debug("confirm usage agreement for application " + application.getName());
                    usageAgreementService.confirmUsageAgreementVersion(application.getId());
                }
            } catch (ApplicationNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
            } catch (AlreadySubscribedException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.ALREADY_SUBSCRIBED, e.getMessage());
            } catch (PermissionDeniedException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.PERMISSION_DENIED, e.getMessage());
            } catch (SubscriptionNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBSCRIPTION_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

            login(request.getID(), response);

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            setStatus(response, e.getErrorCode(), e.getMessage());
            manager.unexport(this);
            return response;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType requestIdentity(WSAuthenticationIdentityRequestType request) {

        LOG.debug("request identity for application: " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {
            if (!confirmationRequired) {
                login(request.getID(), response);
                return response;
            }

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                getApplication();

                IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

                List<AttributeDO> confirmationList = identityService.listIdentityAttributesToConfirm(application.getId(), new Locale(
                        language));
                List<AttributeType> confirmationAttributes = getAttributes(confirmationList, false);
                AssertionType attributeAssertion = getAttributeAssertion(confirmationAttributes);
                response.getAssertion().add(attributeAssertion);

            } catch (SubscriptionNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBSCRIPTION_NOT_FOUND, e.getMessage());
            } catch (ApplicationNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
            } catch (ApplicationIdentityNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_IDENTITY_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), e.getErrorCode(), e.getMessage());
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType confirmIdentity(WSAuthenticationIdentityConfirmationType request) {

        LOG.debug("confirm application identity: " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        Confirmation confirmation = Confirmation.getConfirmation(request.getConfirmation());
        if (Confirmation.REJECT == confirmation)
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.AUTHENTICATION_FAILED, null);

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                getApplication();

                // confirm application identity
                IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

                identityService.confirmIdentity(application.getId());

            } catch (SubscriptionNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBSCRIPTION_NOT_FOUND, e.getMessage());
            } catch (ApplicationNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
            } catch (ApplicationIdentityNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_IDENTITY_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

            login(request.getID(), response);

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            setStatus(response, e.getErrorCode(), e.getMessage());
            manager.unexport(this);
            return response;
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType requestMissingAttributes(WSAuthenticationMissingAttributesRequestType request) {

        LOG.debug("request missing attributes for application: " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {
            if (!hasMissingAttributes) {
                login(request.getID(), response);
                return response;
            }

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                getApplication();

                IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

                List<AttributeDO> missingAttributeList = identityService.listMissingAttributes(application.getId(), new Locale(language));

                // first check if all there are non-user-editable attributes missing, if so user cannot authenticate.
                String unavailableAttributes = "";
                for (AttributeDO missingAttribute : missingAttributeList) {
                    if (!missingAttribute.isEditable()) {
                        unavailableAttributes += missingAttribute.getName() + ", ";
                    }
                }
                if (unavailableAttributes.length() > 0)
                    throw new WSAuthenticationException(WSAuthenticationErrorCode.ATTRIBUTE_UNAVAILABLE, "Attribute(s) ["
                            + unavailableAttributes + "] is(are) unavailable");

                // include optional attributes
                List<AttributeDO> optionalAttributeList = identityService.listOptionalAttributes(application.getId(), new Locale(language));

                List<AttributeType> missingAttributes = getAttributes(missingAttributeList, false);
                List<AttributeType> optionalAttributes = getAttributes(optionalAttributeList, true);
                missingAttributes.addAll(optionalAttributes);

                AssertionType attributeAssertion = getAttributeAssertion(missingAttributes);
                response.getAssertion().add(attributeAssertion);

            } catch (ApplicationNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
            } catch (ApplicationIdentityNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_IDENTITY_NOT_FOUND, e.getMessage());
            } catch (PermissionDeniedException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.PERMISSION_DENIED, e.getMessage());
            } catch (AttributeTypeNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.ATTRIBUTE_TYPE_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            manager.unexport(this);
            return createAuthenticationResponse(request.getID(), e.getErrorCode(), e.getMessage());
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType saveMissingAttributes(WSAuthenticationMissingAttributesSaveRequestType request) {

        LOG.debug("save missing attributes for: " + applicationName);

        if (null == authenticatedSubject) {
            LOG.error("user not yet authenticated");
            return createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        }

        WSAuthenticationResponseType response = createAuthenticationResponse(request.getID(), WSAuthenticationErrorCode.SUCCESS, null);

        try {

            // JAAS Login
            LoginContext loginContext = jaasLogin();
            try {
                IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

                for (AttributeType attribute : request.getAttribute()) {
                    LOG.debug("save attribute: " + attribute.getName());
                    identityService.saveAttribute(attribute);
                }

            } catch (PermissionDeniedException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.PERMISSION_DENIED, e.getMessage());
            } catch (AttributeTypeNotFoundException e) {
                throw new WSAuthenticationException(WSAuthenticationErrorCode.ATTRIBUTE_TYPE_NOT_FOUND, e.getMessage());
            } finally {
                // JAAS Logout
                jaasLogout(loginContext);
            }

            login(request.getID(), response);

        } catch (WSAuthenticationException e) {
            LOG.error("exception: errorCode=" + e.getErrorCode().getErrorCode() + " errorMessage=" + e.getMessage());
            setStatus(response, e.getErrorCode(), e.getMessage());
            manager.unexport(this);
            return response;
        }

        return response;

    }

    /**
     * Proxies the request to the specified device authentication web service.
     */
    private WSAuthenticationResponseType proxyRequest(WSAuthenticationRequestType request)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        if (null == deviceAuthenticationClient) {
            setDeviceAuthenticationClient(request.getDeviceName());
        }

        return deviceAuthenticationClient.authenticate(request);
    }

    /**
     * Check if any authentication steps need to be performed, if not generate authentication assertion and add to specified response.
     */
    private void login(String id, WSAuthenticationResponseType response)
            throws WSAuthenticationException {

        // JAAS Login
        LoginContext loginContext = jaasLogin();

        try {
            // perform device policy check
            boolean devicePolicyCheck = performDevicePolicyCheck();
            if (false == devicePolicyCheck) {
                LOG.error("device policy check failed");
                setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_DEVICE, null);
                manager.unexport(this);
                return;
            }

            // perform global usage agreement check
            performGlobalUsageAgreementCheck();
            if (true == globalConfirmationRequired) {
                response.setAuthenticationStep(AuthenticationStep.GLOBAL_USAGE_AGREEMENT.getValue());
                return;
            }

            // perform subscription check
            performSubscriptionCheck();
            if (true == subscriptionRequired) {
                response.setAuthenticationStep(AuthenticationStep.USAGE_AGREEMENT.getValue());
                return;
            }

            // perform identity confirmation check
            performConfirmationCheck();
            if (true == confirmationRequired) {
                response.setAuthenticationStep(AuthenticationStep.IDENTITY_CONFIRMATION.getValue());
                return;
            }

            // perform missing attributes check
            performMissingAttributesCheck();
            if (true == hasMissingAttributes) {
                response.setAuthenticationStep(AuthenticationStep.MISSING_ATTRIBUTES.getValue());
                return;
            }

        } finally {
            // JAAS Logout
            jaasLogout(loginContext);
        }

        // No further steps needed, generate assertion
        String applicationUserId = getApplicationUserId();

        response.setUserId(applicationUserId);
        response.setDeviceName(authenticatedDevice.getName());
        // AssertionType assertion = generateAssertion(id, applicationUserId);
        AssertionType assertion = getAssertion(id, applicationUserId);
        response.getAssertion().add(assertion);
        manager.unexport(this);
    }

    /**
     * Returns the OLAS device entity for the authentication device specified in the authentication response.
     */
    private DeviceEntity getAuthenticatedDevice(WSAuthenticationResponseType response)
            throws WSAuthenticationException {

        DevicePolicyService devicePolicyService = EjbUtils.getEJB(DevicePolicyService.JNDI_BINDING, DevicePolicyService.class);
        DeviceEntity device;
        try {
            device = devicePolicyService.getDevice(response.getDeviceName());
        } catch (DeviceNotFoundException e) {
            throw new WSAuthenticationException(WSAuthenticationErrorCode.DEVICE_NOT_FOUND, e.getMessage());
        }
        return device;
    }

    private void getApplication()
            throws ApplicationNotFoundException {

        LOG.debug("applicationName = " + applicationName);

        if (null == applicationName)
            throw new ApplicationNotFoundException();

        if (null == application) {
            LOG.debug("a");
            ApplicationService applicationService = EjbUtils.getEJB(ApplicationService.JNDI_BINDING, ApplicationService.class);
            LOG.debug("b");
            application = applicationService.getApplication(applicationName);
            LOG.debug("c");
        }

        LOG.debug("application = " + application);
    }

    /**
     * Checks whether the response contains a valid SAML v2.0 assertion. If so lookup the OLAS subject entity from the value of the
     * assertion's subject.
     */
    private SubjectEntity findSubject(WSAuthenticationResponseType response)
            throws WSAuthenticationException {

        if (null == response.getUserId())
            return null;

        // lookup subject
        NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(NodeAuthenticationService.JNDI_BINDING,
                NodeAuthenticationService.class);
        NodeMappingService nodeMappingService = EjbUtils.getEJB(NodeMappingService.JNDI_BINDING, NodeMappingService.class);
        SubjectService subjectService = EjbUtils.getEJB(SubjectService.JNDI_BINDING, SubjectService.class);
        DevicePolicyService devicePolicyService = EjbUtils.getEJB(DevicePolicyService.JNDI_BINDING, DevicePolicyService.class);

        SubjectEntity subjectEntity;
        try {
            NodeEntity localNode = nodeAuthenticationService.getLocalNode();
            DeviceEntity device = devicePolicyService.getDevice(response.getDeviceName());
            if (device.getLocation().equals(localNode)) {
                subjectEntity = subjectService.getSubject(response.getUserId());
            } else {
                NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(response.getUserId());
                subjectEntity = nodeMapping.getSubject();
            }
        } catch (NodeNotFoundException e) {
            LOG.debug("subject not found");
            throw new WSAuthenticationException(WSAuthenticationErrorCode.NODE_NOT_FOUND, e.getMessage());
        } catch (DeviceNotFoundException e) {
            LOG.debug("device not found: " + response.getDeviceName());
            throw new WSAuthenticationException(WSAuthenticationErrorCode.DEVICE_NOT_FOUND, e.getMessage());
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject not found");
            throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBJECT_NOT_FOUND, e.getMessage());
        } catch (NodeMappingNotFoundException e) {
            LOG.debug("node mapping not found");
            throw new WSAuthenticationException(WSAuthenticationErrorCode.NODE_MAPPING_NOT_FOUND, e.getMessage());
        }

        return subjectEntity;
    }

    private void setDeviceAuthenticationClient(String deviceName)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        DevicePolicyService devicePolicyService = EjbUtils.getEJB(DevicePolicyService.JNDI_BINDING, DevicePolicyService.class);
        String deviceAuthenticationWSURL;
        try {
            deviceAuthenticationWSURL = devicePolicyService.getAuthenticationWSURL(deviceName);
        } catch (DeviceNotFoundException e) {
            LOG.debug("device not found: " + deviceName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.DEVICE_NOT_FOUND, e.getMessage());
        }
        GetDeviceAuthenticationClient getDeviceAuthenticationClient = new GetDeviceAuthenticationClientImpl(deviceAuthenticationWSURL);

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();
        deviceAuthenticationClient = new DeviceAuthenticationClientImpl(getDeviceAuthenticationClient.getInstance(),
                nodeKeyStore.getCertificate(), nodeKeyStore.getPrivateKey());
    }

    /**
     * Performs JAAS login using subject's userId
     * 
     * @return the JAAS LoginContext
     */
    private LoginContext jaasLogin()
            throws WSAuthenticationException {

        if (null == authenticatedSubject)
            return null;

        UsernamePasswordHandler handler = new UsernamePasswordHandler(authenticatedSubject.getUserId(), null);
        try {
            LoginContext loginContext = new LoginContext("client-login", handler);
            LOG.debug("login to client-login with " + authenticatedSubject.getUserId());
            loginContext.login();
            return loginContext;
        } catch (LoginException e) {
            LOG.error("login error: " + e.getMessage(), e);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getMessage());
        }
    }

    /**
     * Performs JAAS logout
     * 
     * @param loginContext
     *            the JAAS login context
     */
    private void jaasLogout(LoginContext loginContext)
            throws WSAuthenticationException {

        LOG.debug("logout " + authenticatedSubject.getUserId());
        try {
            loginContext.logout();
        } catch (LoginException e) {
            LOG.error("logout error: " + e.getMessage(), e);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getMessage());
        }

        flushCredentialCache(authenticatedSubject.getUserId(), SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
    }

    private void flushCredentialCache(String login, String securityDomain) {

        LOG.debug("flush credential cache for " + login + " on security domain " + securityDomain);
        Principal user = new SimplePrincipal(login);
        ObjectName jaasMgr;
        try {
            jaasMgr = new ObjectName("jboss.security:service=JaasSecurityManager");
        } catch (MalformedObjectNameException e) {
            String msg = "ObjectName error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("NPE: " + e.getMessage(), e);
        }
        Object[] params = { securityDomain, user };
        String[] signature = { String.class.getName(), Principal.class.getName() };
        MBeanServer server = MBeanServerLocator.locateJBoss();
        try {
            server.invoke(jaasMgr, "flushAuthenticationCache", params, signature);
        } catch (InstanceNotFoundException e) {
            String msg = "instance not found: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (MBeanException e) {
            String msg = "mbean error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        } catch (ReflectionException e) {
            String msg = "reflection error: " + e.getMessage();
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Check whether the used authentication device is sufficient for the given application.
     */
    private boolean performDevicePolicyCheck()
            throws WSAuthenticationException {

        DevicePolicyService devicePolicyService = EjbUtils.getEJB(DevicePolicyService.JNDI_BINDING, DevicePolicyService.class);
        List<DeviceEntity> devicePolicy;
        try {
            getApplication();
            if (null == application) {
                LOG.debug("que");
            }

            devicePolicy = devicePolicyService.getDevicePolicy(application.getId(), null);
        } catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
        } catch (EmptyDevicePolicyException e) {
            LOG.error("empty device policy for " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.EMPTY_DEVICE_POLICY, e.getMessage());
        }
        if (devicePolicy.contains(authenticatedDevice))
            return true;

        return false;
    }

    /**
     * Checks whether the authenticated user has to accept a global OLAS usage agreement.
     */
    private void performGlobalUsageAgreementCheck() {

        UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING, UsageAgreementService.class);
        globalConfirmationRequired = usageAgreementService.requiresGlobalUsageAgreementAcceptation(language);
    }

    /**
     * Checks whether the authenticated user has to subscribe to or accept the application's usage agreement.
     */
    private void performSubscriptionCheck()
            throws WSAuthenticationException {

        SubscriptionService subscriptionService = EjbUtils.getEJB(SubscriptionService.JNDI_BINDING, SubscriptionService.class);
        UsageAgreementService usageAgreementService = EjbUtils.getEJB(UsageAgreementService.JNDI_BINDING, UsageAgreementService.class);

        try {
            getApplication();

            subscriptionRequired = !subscriptionService.isSubscribed(application.getId());
            if (!subscriptionRequired) {
                subscriptionRequired = usageAgreementService.requiresUsageAgreementAcceptation(application.getId(), language);
            }
        } catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
        } catch (SubscriptionNotFoundException e) {
            LOG.error("subscription not found for " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBSCRIPTION_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Checks whether the authenticated user has to confirm the application's identity.
     */
    private void performConfirmationCheck()
            throws WSAuthenticationException {

        IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

        try {
            getApplication();

            confirmationRequired = identityService.isConfirmationRequired(application.getId());
        } catch (SubscriptionNotFoundException e) {
            LOG.error("subscription not found for " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.SUBSCRIPTION_NOT_FOUND, e.getMessage());
        } catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
        } catch (ApplicationIdentityNotFoundException e) {
            LOG.error("application identity not found for " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_IDENTITY_NOT_FOUND, e.getMessage());
        }
        LOG.debug("confirmation required: " + confirmationRequired);
    }

    /**
     * Checks whether the authenticated user needs to provide missing attributes for the specified application's identity.
     */
    private void performMissingAttributesCheck()
            throws WSAuthenticationException {

        IdentityService identityService = EjbUtils.getEJB(IdentityService.JNDI_BINDING, IdentityService.class);

        try {
            getApplication();

            hasMissingAttributes = identityService.hasMissingAttributes(application.getId());
        } catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
        } catch (ApplicationIdentityNotFoundException e) {
            LOG.error("application identity not found for " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_IDENTITY_NOT_FOUND, e.getMessage());
        } catch (PermissionDeniedException e) {
            LOG.error("permission denied: " + e.getMessage());
            throw new WSAuthenticationException(WSAuthenticationErrorCode.PERMISSION_DENIED, e.getMessage());
        } catch (AttributeTypeNotFoundException e) {
            LOG.error("attribute type not found: " + e.getMessage());
            throw new WSAuthenticationException(WSAuthenticationErrorCode.ATTRIBUTE_TYPE_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Set that authentication response status.
     */
    private void setStatus(WSAuthenticationResponseType response, WSAuthenticationErrorCode code, String message) {

        StatusType status = new StatusType();
        StatusCodeType statusCode = new StatusCodeType();
        statusCode.setValue(code.getErrorCode());
        status.setStatusCode(statusCode);
        status.setStatusMessage(message);
        response.setStatus(status);
    }

    /**
     * Create basic global usage agreement response
     */
    private WSAuthenticationGlobalUsageAgreementResponseType createGlobalUsageAgreementResponse(String id, WSAuthenticationErrorCode code,
                                                                                                String message) {

        WSAuthenticationGlobalUsageAgreementResponseType response = new WSAuthenticationGlobalUsageAgreementResponseType();
        setResponse(response, id, code, message);
        return response;
    }

    /**
     * Create basic application usage agreement response
     */
    private WSAuthenticationUsageAgreementResponseType createUsageAgreementResponse(String id, WSAuthenticationErrorCode code,
                                                                                    String message) {

        WSAuthenticationUsageAgreementResponseType response = new WSAuthenticationUsageAgreementResponseType();
        setResponse(response, id, code, message);
        return response;
    }

    /**
     * Create basic authentication response
     */
    private WSAuthenticationResponseType createAuthenticationResponse(String id, WSAuthenticationErrorCode code, String message) {

        WSAuthenticationResponseType response = new WSAuthenticationResponseType();
        setResponse(response, id, code, message);
        return response;
    }

    private void setResponse(WSAuthenticationResponseType response, String id, WSAuthenticationErrorCode code, String message) {

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("datatype config error");
        }

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date());
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }

        // Issuer
        NameIDType issuerName = new NameIDType();
        String samlAuthorityIssuerName = samlAuthorityService.getIssuerName();
        issuerName.setValue(samlAuthorityIssuerName);

        response.setID(idGenerator.generateIdentifier());
        response.setVersion(SAMLVersion.VERSION_20.toString());
        response.setIssueInstant(now);
        response.setInResponseTo(id);
        response.setIssuer(issuerName);

        if (null != authenticatedDevice) {
            response.setDeviceName(authenticatedDevice.getName());
        }

        setStatus(response, code, message);
    }

    /**
     * Get application user ID.
     */
    private String getApplicationUserId()
            throws WSAuthenticationException {

        UserIdMappingService userIdMappingService = EjbUtils.getEJB(UserIdMappingService.JNDI_BINDING, UserIdMappingService.class);

        try {
            getApplication();
            return userIdMappingService.getApplicationUserId(application, authenticatedSubject);
        } catch (ApplicationNotFoundException e) {
            LOG.error("application not found: " + applicationName);
            throw new WSAuthenticationException(WSAuthenticationErrorCode.APPLICATION_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Generate basic SAML v2.0 assertion. No SubjectConfirmation or AttributeStatements are added at this point.
     */
    private AssertionType generateBaseAssertion()
            throws WSAuthenticationException {

        if (null == authenticatedSubject)
            throw new WSAuthenticationException(WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);
        if (null == authenticatedDevice)
            throw new WSAuthenticationException(WSAuthenticationErrorCode.NOT_AUTHENTICATED, null);

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("datatype config error");
        }

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date());
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }

        // Assertion
        AssertionType assertion = new AssertionType();
        assertion.setVersion(SAMLVersion.VERSION_20.toString());
        assertion.setID(idGenerator.generateIdentifier());
        assertion.setIssueInstant(now);

        // Issuer
        NameIDType issuerName = new NameIDType();
        String samlAuthorityIssuerName = samlAuthorityService.getIssuerName();
        issuerName.setValue(samlAuthorityIssuerName);
        assertion.setIssuer(issuerName);

        return assertion;
    }

    private AssertionType getAssertion(String id, String applicationUserId)
            throws WSAuthenticationException {

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        PrivateKeyEntry olasKeyPair = SafeOnlineNodeKeyStore.getPrivateKeyEntry();

        Map<DateTime, String> authentications = new HashMap<DateTime, String>();
        authentications.put(new DateTime(), authenticatedDevice.getName());

        Element assertionElement = null;
        if (null != keyInfo) {
            // holder-of-key : SAML assertion SHOULD contain a <ds:signature> element that protects the integrity of the confirmation
            // <ds:KeyInfo> established by the assertion authority.
            Assertion assertion = Saml2Util.getAssertion(id, applicationName, applicationUserId, samlAuthorityService.getIssuerName(),
                    samlAuthorityService.getAuthnAssertionValidity(), null, authentications, Saml2SubjectConfirmationMethod.HOLDER_OF_KEY,
                    null);
            assertionElement = Saml2Util.sign(assertion, olasKeyPair.getCertificate().getPublicKey(), olasKeyPair.getPrivateKey());

        } else {
            Assertion assertion = Saml2Util.getAssertion(id, applicationName, applicationUserId, samlAuthorityService.getIssuerName(),
                    samlAuthorityService.getAuthnAssertionValidity(), null, authentications, Saml2SubjectConfirmationMethod.SENDER_VOUCHES,
                    null);
            MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
            try {
                assertionElement = marshaller.marshall(assertion);
            } catch (MarshallingException e) {
                throw new RuntimeException("opensaml2 marshalling error: " + e.getMessage(), e);
            }

        }
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(AssertionType.class);
            javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<?> assertionObject = (JAXBElement<?>) unmarshaller.unmarshal(assertionElement);
            AssertionType assertion = (AssertionType) assertionObject.getValue();
            if (null != keyInfo) {
                org.w3._2000._09.xmldsig_.ObjectFactory dsigObjectFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();
                for (JAXBElement<?> element : assertion.getSubject().getContent()) {
                    if (element.getValue() instanceof SubjectConfirmationType) {
                        SubjectConfirmationType subjectConfirmation = (SubjectConfirmationType) element.getValue();
                        subjectConfirmation.getSubjectConfirmationData().getContent().add(dsigObjectFactory.createKeyInfo(keyInfo));
                    }
                }
            }
            return assertion;
        } catch (JAXBException e) {
            throw new WSAuthenticationException(WSAuthenticationErrorCode.INTERNAL_ERROR, e.getMessage());
        }

    }

    private AssertionType getAttributeAssertion(List<AttributeType> attributes)
            throws WSAuthenticationException {

        AssertionType assertion = generateBaseAssertion();

        List<StatementAbstractType> statements = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        AttributeStatementType attributeStatement = new AttributeStatementType();
        statements.add(attributeStatement);
        List<Object> statementAttributes = attributeStatement.getAttributeOrEncryptedAttribute();

        for (AttributeType attribute : attributes) {
            statementAttributes.add(attribute);
        }

        return assertion;
    }

    private List<AttributeType> getAttributes(List<AttributeDO> attributeList, boolean optional) {

        List<AttributeType> attributes = new LinkedList<AttributeType>();

        if (null == attributeList)
            return attributes;

        for (int idx = 0; idx < attributeList.size(); idx++) {
            AttributeDO attribute = attributeList.get(idx);

            AttributeType statementAttribute = new AttributeType();
            statementAttribute.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
            statementAttribute.setName(attribute.getName());
            statementAttribute.setFriendlyName(attribute.getHumanReadableName());
            statementAttribute.getOtherAttributes().put(WebServiceConstants.DATAMINING_ATTRIBUTE,
                    new Boolean(attribute.isDataMining()).toString());
            statementAttribute.getOtherAttributes().put(WebServiceConstants.DATATYPE_ATTRIBUTE, getDataType(attribute).getValue());
            statementAttribute.getOtherAttributes().put(WebServiceConstants.OPTIONAL_ATTRIBUTE, new Boolean(optional).toString());

            List<Object> attributeValues = statementAttribute.getAttributeValue();

            if (attribute.isCompounded()) {

                for (idx++; idx < attributeList.size(); idx++) {
                    AttributeDO memberAttribute = attributeList.get(idx);
                    if (!memberAttribute.isMember()) {
                        idx--;
                        break;
                    }

                    AttributeType memberAttributeType = new AttributeType();
                    memberAttributeType.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
                    memberAttributeType.setName(memberAttribute.getName());
                    memberAttributeType.setFriendlyName(memberAttribute.getHumanReadableName());
                    memberAttributeType.getOtherAttributes().put(WebServiceConstants.DATAMINING_ATTRIBUTE,
                            new Boolean(memberAttribute.isDataMining()).toString());
                    memberAttributeType.getOtherAttributes().put(WebServiceConstants.DATATYPE_ATTRIBUTE,
                            getDataType(memberAttribute).getValue());
                    memberAttributeType.getOtherAttributes().put(WebServiceConstants.OPTIONAL_ATTRIBUTE, new Boolean(optional).toString());

                    attributeValues.add(memberAttributeType);
                }
            }
            attributes.add(statementAttribute);
        }
        return attributes;

    }

    private DataType getDataType(AttributeDO attribute) {

        if (attribute.getType() == DatatypeType.STRING)
            return DataType.STRING;
        else if (attribute.getType() == DatatypeType.BOOLEAN)
            return DataType.BOOLEAN;
        else if (attribute.getType() == DatatypeType.DATE)
            return DataType.DATE;
        else if (attribute.getType() == DatatypeType.DOUBLE)
            return DataType.DOUBLE;
        else if (attribute.getType() == DatatypeType.INTEGER)
            return DataType.INTEGER;
        else if (attribute.getType() == DatatypeType.COMPOUNDED)
            return DataType.COMPOUNDED;
        else
            throw new RuntimeException("Unknown datatype " + attribute.getType().getFriendlyName());
    }
}
