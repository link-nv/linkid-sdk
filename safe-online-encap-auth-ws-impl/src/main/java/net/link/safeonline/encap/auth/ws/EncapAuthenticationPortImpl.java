/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.auth.ws;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth.DeviceAuthenticationPort;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.device.auth.ws.util.DeviceAuthenticationPortUtil;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful Encap Authentication web service using JAX-WS.
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
public class EncapAuthenticationPortImpl implements DeviceAuthenticationPort {

    static final Log                                                  LOG = LogFactory.getLog(EncapAuthenticationPortImpl.class);

    public static StatefulWebServiceManager<DeviceAuthenticationPort> manager;

    private String                                                    mobile;

    private String                                                    challengeId;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    public EncapAuthenticationPortImpl() {

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

        if (null == mobile && null == challengeId)
            // step 1, expect mobile attribute, will send OTP after mobile has been verified
            return verifyMobileAndSendOtp(request);

        // step 2, expect OTP and pin attribute.
        return verifyOtpAndAuthenticate(request);

    }

    private WSAuthenticationResponseType verifyMobileAndSendOtp(WSAuthenticationRequestType request) {

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        String issuerName = samlAuthorityService.getIssuerName();
        WSAuthenticationResponseType response = DeviceAuthenticationPortUtil.generateResponse(request.getID(), issuerName,
                request.getDeviceName());

        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();
        for (NameValuePairType nameValuePair : deviceCredentials.getNameValuePair()) {
            if (nameValuePair.getName().equals(EncapConstants.ENCAP_WS_AUTH_MOBILE_ATTRIBUTE)) {
                mobile = nameValuePair.getValue();
            }
        }

        if (null == mobile) {
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + EncapConstants.ENCAP_WS_AUTH_MOBILE_ATTRIBUTE + "\" is not specified");
            manager.unexport(this);
            return response;
        }

        EncapDeviceService encapDeviceService = EjbUtils.getEJB(EncapDeviceService.JNDI_BINDING, EncapDeviceService.class);
        try {
            encapDeviceService.checkMobile(mobile);
        } catch (SubjectNotFoundException e) {
            LOG.error("subject not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUBJECT_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (AttributeTypeNotFoundException e) {
            LOG.error("attribute type not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.ATTRIBUTE_TYPE_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (AttributeNotFoundException e) {
            LOG.error("attribute not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.ATTRIBUTE_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (DeviceDisabledException e) {
            LOG.error("device disabled: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.DEVICE_DISABLED, e.getMessage());
            manager.unexport(this);
            return response;
        }

        LOG.debug("request OTP for mobile: " + mobile);
        try {
            challengeId = encapDeviceService.requestOTP(mobile);
        } catch (MobileException e) {
            LOG.error("exception while requesting OTP: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.REQUEST_FAILED, e.getMessage());
            manager.unexport(this);
            return response;
        }

        DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUCCESS, null);
        return response;
    }

    private WSAuthenticationResponseType verifyOtpAndAuthenticate(WSAuthenticationRequestType request) {

        SamlAuthorityService samlAuthorityService = EjbUtils.getEJB(SamlAuthorityService.JNDI_BINDING, SamlAuthorityService.class);
        String issuerName = samlAuthorityService.getIssuerName();
        WSAuthenticationResponseType response = DeviceAuthenticationPortUtil.generateResponse(request.getID(), issuerName,
                request.getDeviceName());

        String otp = null;
        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();
        for (NameValuePairType nameValuePair : deviceCredentials.getNameValuePair()) {
            if (nameValuePair.getName().equals(EncapConstants.ENCAP_WS_AUTH_OTP_ATTRIBUTE)) {
                otp = nameValuePair.getValue();
            }
        }

        if (null == otp) {
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + EncapConstants.ENCAP_WS_AUTH_OTP_ATTRIBUTE + "\" is not specified");
            manager.unexport(this);
            return response;
        }

        EncapDeviceService encapDeviceService = EjbUtils.getEJB(EncapDeviceService.JNDI_BINDING, EncapDeviceService.class);

        String userId;
        try {
            userId = encapDeviceService.authenticate(mobile, challengeId, otp);
        } catch (SubjectNotFoundException e) {
            LOG.error("subject not found: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.SUBJECT_NOT_FOUND, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (DeviceAuthenticationException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (MobileException e) {
            LOG.error("authentication failed: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getMessage());
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
