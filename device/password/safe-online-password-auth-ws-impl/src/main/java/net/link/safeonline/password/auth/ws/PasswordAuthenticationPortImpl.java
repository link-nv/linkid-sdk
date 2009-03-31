/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.auth.ws;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.DeviceAuthenticationPort;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.device.auth.ws.util.DeviceAuthenticationPortUtil;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful Password Authentication web service using JAX-WS.
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
@WebService(endpointInterface = "net.lin_k.safe_online.auth.DeviceAuthenticationPort")
@HandlerChain(file = "auth-ws-handlers.xml")
public class PasswordAuthenticationPortImpl implements DeviceAuthenticationPort {

    static final Log                                                  LOG = LogFactory.getLog(PasswordAuthenticationPortImpl.class);

    public static StatefulWebServiceManager<DeviceAuthenticationPort> manager;

    private NameIdentifierMappingClient                               idMappingClient;

    private String                                                    wsLocation;


    @PostConstruct
    public void postConstructCallback() {

        try {
            Context ctx = new javax.naming.InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            wsLocation = (String) env.lookup("wsLocation");
        } catch (NamingException e) {
            LOG.debug("naming exception: " + e.getMessage());
            throw new RuntimeException("\"wsLocation\" not specified");
        }

    }

    public PasswordAuthenticationPortImpl() {

        if (null != manager) {
            WSAuthenticationService wsAuthenticationService = EjbUtils.getEJB(WSAuthenticationService.JNDI_BINDING,
                    WSAuthenticationService.class);
            manager.setTimeout(1000 * wsAuthenticationService.getAuthenticationTimeout(), new TimeoutCallback());
        }
    }

    public PasswordAuthenticationPortImpl(NameIdentifierMappingClient idMappingClient) {

        this();
        this.idMappingClient = idMappingClient;
    }


    class TimeoutCallback implements StatefulWebServiceManager.Callback<DeviceAuthenticationPort> {

        /**
         * {@inheritDoc}
         */
        public void onTimeout(DeviceAuthenticationPort timedOutObject, StatefulWebServiceManager<DeviceAuthenticationPort> serviceManager) {

            LOG.debug("instance timed out");
        }

    }


    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request) {

        LOG.debug("authenticate");

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        String issuerName = samlAuthorityService.getIssuerName();

        WSAuthenticationResponseType response = DeviceAuthenticationPortUtil.generateResponse(request.getID(), issuerName,
                request.getDeviceName());

        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();
        String loginName = null;
        String password = null;
        for (NameValuePairType nameValuePair : deviceCredentials.getNameValuePair()) {
            if (nameValuePair.getName().equals(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE)) {
                loginName = nameValuePair.getValue();
            } else if (nameValuePair.getName().equals(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE)) {
                password = nameValuePair.getValue();
            }
        }
        if (null == loginName || null == password) {
            LOG.error("insufficient device credentials");
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE + "\" or \""
                    + PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE + "\" or both are not specified");
            return response;
        }

        try {
            PasswordDeviceService passwordDeviceService = EjbUtils.getEJB(PasswordDeviceService.JNDI_BINDING, PasswordDeviceService.class);
            String userId = getUserId(loginName);
            passwordDeviceService.authenticate(userId, password);

            response.setUserId(userId);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUCCESS, null);

            return response;
        }

        catch (DeviceAuthenticationException e) {
            LOG.debug("authentication failed");
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getMessage());
        } catch (SubjectNotFoundException e) {
            LOG.error("subject not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUBJECT_NOT_FOUND, e.getMessage());
        } catch (DeviceDisabledException e) {
            LOG.error("device disabled: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.DEVICE_DISABLED, e.getMessage());
        } catch (PermissionDeniedException e) {
            LOG.error("permission denied: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PERMISSION_DENIED, e.getMessage());
        } catch (DeviceRegistrationNotFoundException e) {
            LOG.error("device not registered: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.DEVICE_NOT_FOUND, e.getMessage());
        }

        finally {
            // authentication finished, cleanup
            manager.unexport(this);
        }

        return response;
    }

    /**
     * Retrieves userId of specified loginName using ID mapping WS
     * 
     */
    private String getUserId(String loginName)
            throws SubjectNotFoundException, PermissionDeniedException {

        if (null == idMappingClient) {
            SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();
            idMappingClient = new NameIdentifierMappingClientImpl(wsLocation, nodeKeyStore.getCertificate(), nodeKeyStore.getPrivateKey());
        }

        String userId;
        try {
            userId = idMappingClient.getUserId(loginName);
        } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
            LOG.error("subject not found: " + loginName);
            throw new SubjectNotFoundException();
        } catch (RequestDeniedException e) {
            LOG.error("request denied: " + e.getMessage());
            throw new PermissionDeniedException(e.getMessage());
        } catch (WSClientTransportException e) {
            LOG.error("failed to contact web service: " + e.getMessage());
            throw new PermissionDeniedException(e.getMessage());
        }
        return userId;

    }

}
