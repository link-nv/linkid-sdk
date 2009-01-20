/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.auth.ws;

import java.net.ConnectException;

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
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.auth.ws.util.DeviceAuthenticationPortUtil;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.model.otpoversms.OtpService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;


/**
 * Implementation of OLAS Stateful OTP over SMS Authentication web service using JAX-WS.
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
public class OtpOverSmsAuthenticationPortImpl implements DeviceAuthenticationPort {

    static final Log                                                  LOG = LogFactory.getLog(OtpOverSmsAuthenticationPortImpl.class);

    public static StatefulWebServiceManager<DeviceAuthenticationPort> manager;

    private String                                                    mobile;

    private OtpService                                                otpService;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    public OtpOverSmsAuthenticationPortImpl() {

        // XXX: make this configurable ..., time is in ms
        if (null != manager) {
            manager.setTimeout(1000 * 60 * 30, new TimeoutCallback());
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

        if (null == this.mobile && null == this.otpService)
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
            if (nameValuePair.getName().equals(OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_MOBILE_ATTRIBUTE)) {
                this.mobile = nameValuePair.getValue();
            }
        }

        if (null == this.mobile) {
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_MOBILE_ATTRIBUTE + "\" is not specified");
            manager.unexport(this);
            return response;
        }

        OtpOverSmsDeviceService otpOverSmsDeviceService = EjbUtils.getEJB(OtpOverSmsDeviceService.JNDI_BINDING,
                OtpOverSmsDeviceService.class);
        try {
            otpOverSmsDeviceService.checkMobile(this.mobile);
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

        LOG.debug("request OTP for mobile: " + this.mobile);
        try {
            this.otpService = otpOverSmsDeviceService.requestOtp(this.mobile);
        } catch (ConnectException e) {
            LOG.error("connection exception while sending OTP: " + e.getMessage(), e);
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.REQUEST_FAILED, e.getMessage());
            manager.unexport(this);
            return response;
        } catch (SafeOnlineResourceException e) {
            LOG.error("exception while sending OTP: " + e.getMessage(), e);
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
        String pin = null;
        DeviceCredentialsType deviceCredentials = request.getDeviceCredentials();
        for (NameValuePairType nameValuePair : deviceCredentials.getNameValuePair()) {
            if (nameValuePair.getName().equals(OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_OTP_ATTRIBUTE)) {
                otp = nameValuePair.getValue();
            } else if (nameValuePair.getName().equals(OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_PIN_ATTRIBUTE)) {
                pin = nameValuePair.getValue();
            }
        }

        if (null == pin || null == otp) {
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.INSUFFICIENT_CREDENTIALS, "\""
                    + OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_OTP_ATTRIBUTE + "\" or \""
                    + OtpOverSmsConstants.OTPOVERSMS_WS_AUTH_PIN_ATTRIBUTE + "\" is not specified");
            manager.unexport(this);
            return response;
        }

        OtpOverSmsDeviceService otpOverSmsDeviceService = EjbUtils.getEJB(OtpOverSmsDeviceService.JNDI_BINDING,
                OtpOverSmsDeviceService.class);

        boolean verified;
        try {
            verified = otpOverSmsDeviceService.verifyOtp(this.otpService, this.mobile, otp);
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

        if (!verified) {
            LOG.error("authentication failed, otp verification failed");
            DeviceAuthenticationPortUtil.setStatus(response, WSAuthenticationErrorCode.AUTHENTICATION_FAILED, "OTP verification failed");
            manager.unexport(this);
            return response;
        }

        String userId;
        try {
            userId = otpOverSmsDeviceService.authenticate(this.mobile, pin);
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
