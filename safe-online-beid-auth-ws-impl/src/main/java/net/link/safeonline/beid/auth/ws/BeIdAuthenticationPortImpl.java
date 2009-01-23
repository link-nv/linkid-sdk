/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.auth.ws;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.lin_k.safe_online.auth.DeviceAuthenticationPort;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.device.auth.ws.util.DeviceAuthenticationPortUtil;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.util.encoders.Hex;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful BeId Authentication web service using JAX-WS.
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
public class BeIdAuthenticationPortImpl implements DeviceAuthenticationPort {

    static final Log                                                  LOG = LogFactory.getLog(BeIdAuthenticationPortImpl.class);

    public static StatefulWebServiceManager<DeviceAuthenticationPort> manager;

    private String                                                    sessionId;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    public BeIdAuthenticationPortImpl() {

        if (null != manager) {
            WSAuthenticationService wsAuthenticationService = EjbUtils.getEJB(WSAuthenticationService.JNDI_BINDING,
                    WSAuthenticationService.class);
            manager.setTimeout(1000 * wsAuthenticationService.getAuthenticationTimeout(), new TimeoutCallback());
        }
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

        if (null == sessionId)
            // step 1, generate sessionId and return in response
            return generateSessionId(request);

        // step 2, expect authentication statement
        return beidAuthentication(request);

    }

    private String generateIdentifier(int size)
            throws NoSuchAlgorithmException {

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] buf = new byte[size];
        random.nextBytes(buf);
        return new String(Hex.encode(buf));
    }

    private WSAuthenticationResponseType generateSessionId(WSAuthenticationRequestType request) {

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        String issuerName = samlAuthorityService.getIssuerName();
        WSAuthenticationResponseType response = DeviceAuthenticationPortUtil.generateResponse(request.getID(), issuerName,
                request.getDeviceName());

        try {
            sessionId = generateIdentifier(16);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("no such algorithm exception: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INTERNAL_ERROR, e.getMessage());
            manager.unexport(this);
            return response;
        }

        DeviceAuthenticationInformationType deviceAuthenticationInformation = new DeviceAuthenticationInformationType();
        NameValuePairType nameValuePair = new NameValuePairType();
        nameValuePair.setName(BeIdConstants.BEID_WS_AUTH_SESSION_ID_ATTRIBUTE);
        nameValuePair.setValue(sessionId);
        deviceAuthenticationInformation.getNameValuePair().add(nameValuePair);
        response.setDeviceAuthenticationInformation(deviceAuthenticationInformation);

        DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUCCESS, null);
        return response;
    }

    private WSAuthenticationResponseType beidAuthentication(WSAuthenticationRequestType request) {

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        String issuerName = samlAuthorityService.getIssuerName();
        WSAuthenticationResponseType response = DeviceAuthenticationPortUtil.generateResponse(request.getID(), issuerName,
                request.getDeviceName());

        String statementData = null;
        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();
        for (NameValuePairType nameValuePair : deviceCredentials.getNameValuePair()) {
            if (nameValuePair.getName().equals(BeIdConstants.BEID_WS_AUTH_STATEMENT_ATTRIBUTE)) {
                statementData = nameValuePair.getValue();
            }
        }

        if (null == statementData) {
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + BeIdConstants.BEID_WS_AUTH_STATEMENT_ATTRIBUTE + "\" is not specified");
            manager.unexport(this);
            return response;
        }

        AuthenticationStatement authenticationStatement;
        try {
            authenticationStatement = new AuthenticationStatement(Base64.decode(statementData));
        } catch (DecodingException e) {
            LOG.error("decoding exception: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INVALID_CREDENTIALS, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (Base64DecodingException e) {
            LOG.error("base64 decoding exception: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INVALID_CREDENTIALS, e.getMessage());
            manager.unexport(this);
            return response;
        }

        BeIdDeviceService beidDeviceService = EjbUtils.getEJB(BeIdDeviceService.JNDI_BINDING, BeIdDeviceService.class);

        String userId;
        try {
            userId = beidDeviceService.authenticate(sessionId, request.getApplicationId(), authenticationStatement);
        } catch (SubjectNotFoundException e) {
            LOG.error("subject not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUBJECT_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (DeviceNotFoundException e) {
            LOG.error("device not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.DEVICE_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (TrustDomainNotFoundException e) {
            LOG.error("trust domain not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INVALID_CREDENTIALS, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (ArgumentIntegrityException e) {
            LOG.error("invalid credentials: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INVALID_CREDENTIALS, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (PkiRevokedException e) {
            LOG.error("PKI revoked: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PKI_REVOKED, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (PkiSuspendedException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PKI_SUSPENDED, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (PkiExpiredException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PKI_EXPIRED, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (PkiNotYetValidException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PKI_NOT_YET_VALID, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (PkiInvalidException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.PKI_INVALID, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (DeviceDisabledException e) {
            LOG.error("device disabled: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.DEVICE_DISABLED, e.getMessage());
            manager.unexport(this);
            return response;
        }

        if (null != userId) {
            response.setUserId(userId);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUCCESS, null);
        } else {
            LOG.debug("authentication failed");
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.AUTHENTICATION_FAILED, null);
        }

        // authentication finished, cleanup
        manager.unexport(this);

        return response;
    }
}
