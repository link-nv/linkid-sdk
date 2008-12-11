/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import java.net.ConnectException;
import java.security.SecureRandom;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.model.otpoversms.OtpService;
import net.link.safeonline.model.otpoversms.OtpServiceRemote;
import net.link.safeonline.osgi.OSGIHostActivator;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.sms.SmsService;

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

    private final static Log LOG = LogFactory.getLog(OtpServiceBean.class);

    // @Configurable(name = "Mobile OTP Server", group = "Mobile OTP")
    // private String otpServerLocation = "http://localhost:8080/safe-online-sms-ws/dummy";

    @Configurable(name = OSGIHostActivator.SMS_SERVICE_IMPL_NAME, group = OSGIHostActivator.SMS_SERVICE_GROUP_NAME, multipleChoice = true)
    private String           smsServiceName;

    private String           expectedOtp;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable    osgiStartable;


    /**
     * {@inheritDoc}
     */
    public void requestOtp(String mobile)
            throws ConnectException, SafeOnlineResourceException {

        LOG.debug("request otp for mobile " + mobile + " using sms service: " + this.smsServiceName);

        SecureRandom random = new SecureRandom();
        this.expectedOtp = Integer.toString(Math.abs(random.nextInt()));

        SmsService smsService = this.osgiStartable.getSmsService(this.smsServiceName);
        smsService.sendSms(mobile, this.expectedOtp);
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(String otp) {

        LOG.debug("verify otp " + otp);

        return this.expectedOtp.equals(otp);
    }

}
