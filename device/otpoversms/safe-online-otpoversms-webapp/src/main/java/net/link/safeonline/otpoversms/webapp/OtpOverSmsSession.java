/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;


/**
 * <h2>{@link OtpOverSmsSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 4, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OtpOverSmsSession extends WebSession {

    private static final long       serialVersionUID = 1L;
    private OtpOverSmsDeviceService otpOverSmsDeviceService;


    public OtpOverSmsSession(Request request) {

        super(request);
    }

    public static OtpOverSmsSession get() {

        return (OtpOverSmsSession) Session.get();
    }

    /**
     * Remember the given {@link OtpOverSmsDeviceService} bean on the session.
     */
    public void setDeviceBean(OtpOverSmsDeviceService otpOverSmsDeviceService) {

        this.otpOverSmsDeviceService = otpOverSmsDeviceService;
    }

    /**
     * @return The {@link OtpOverSmsDeviceService} of the session.
     */
    public OtpOverSmsDeviceService getDeviceService() {

        return otpOverSmsDeviceService;
    }

    public boolean isChallenged() {

        return otpOverSmsDeviceService != null && otpOverSmsDeviceService.isChallenged();
    }
}
