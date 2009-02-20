/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms.bean;

import static net.link.safeonline.osgi.OSGIConstants.SMS_SERVICE_GROUP_NAME;
import static net.link.safeonline.osgi.OSGIConstants.SMS_SERVICE_IMPL_NAME;

import java.net.ConnectException;
import java.security.SecureRandom;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.ResourceAuditLoggerInterceptor;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.model.otpoversms.OtpService;
import net.link.safeonline.model.otpoversms.OtpServiceRemote;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIConstants.OSGIServiceType;
import net.link.safeonline.osgi.sms.SmsService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateful
@LocalBinding(jndiBinding = OtpService.JNDI_BINDING)
@RemoteBinding(jndiBinding = OtpServiceRemote.JNDI_BINDING)
@Interceptors( { ConfigurationInterceptor.class, ResourceAuditLoggerInterceptor.class })
@Configurable
public class OtpServiceBean implements OtpService, OtpServiceRemote {

    private final static Log LOG = LogFactory.getLog(OtpServiceBean.class);

    @Configurable(name = SMS_SERVICE_IMPL_NAME, group = SMS_SERVICE_GROUP_NAME, multipleChoice = true)
    private String           smsServiceName;

    private String           expectedOtp;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable    osgiStartable;


    /**
     * {@inheritDoc}
     */
    public void requestOtp(String mobile)
            throws ConnectException, SafeOnlineResourceException {

        LOG.debug("request otp for mobile " + mobile + " using sms service: " + smsServiceName);

        SecureRandom random = new SecureRandom();
        expectedOtp = Integer.toString(Math.abs(random.nextInt()));

        OSGIService osgiService = osgiStartable.getService(smsServiceName, OSGIServiceType.SMS_SERVICE);
        ((SmsService) osgiService.getService()).sendSms(mobile, expectedOtp);
        osgiService.ungetService();
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyOtp(String otp) {

        LOG.debug("verify otp " + otp);

        return expectedOtp.equals(otp);
    }

}
