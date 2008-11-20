/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.backend.bean;

import java.net.ConnectException;
import java.security.SecureRandom;

import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.device.backend.OtpService;
import net.link.safeonline.device.backend.OtpServiceRemote;
import net.link.safeonline.sdk.ws.otpoversms.SmsClient;
import net.link.safeonline.sdk.ws.otpoversms.SmsClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateful
@LocalBinding(jndiBinding = OtpService.JNDI_BINDING)
@RemoteBinding(jndiBinding = OtpServiceRemote.JNDI_BINDING)
@Interceptors(ConfigurationInterceptor.class)
@Configurable
public class OtpServiceBean implements OtpService, OtpServiceRemote {

    private final static Log LOG               = LogFactory.getLog(OtpServiceBean.class);

    @Configurable(name = "Mobile OTP Server", group = "Mobile OTP")
    private String           otpServerLocation = "http://localhost:8080/safe-online-sms-ws/dummy";

    private String           expectedOtp;


    /**
     * {@inheritDoc}
     */
    public void requestOtp(String mobile)
            throws ConnectException {

        LOG.debug("request otp for mobile " + mobile + " otp server location: " + this.otpServerLocation);

        SecureRandom random = new SecureRandom();
        this.expectedOtp = Integer.toString(Math.abs(random.nextInt()));

        SmsClient smsClient = new SmsClientImpl(this.otpServerLocation);
        smsClient.sendSms(mobile, this.expectedOtp);

    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(String otp) {

        LOG.debug("verify otp " + otp + " otp server location: " + this.otpServerLocation);

        return this.expectedOtp.equals(otp);
    }

}
