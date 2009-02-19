/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.encap.webapp;

import net.link.safeonline.model.encap.EncapDeviceService;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;


/**
 * <h2>{@link EncapSession}<br>
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
public class EncapSession extends WebSession {

    private static final long  serialVersionUID = 1L;
    private EncapDeviceService encapDeviceService;


    public EncapSession(Request request) {

        super(request);
    }

    public static EncapSession get() {

        return (EncapSession) Session.get();
    }

    /**
     * Remember the given {@link EncapDeviceService} bean on the session.
     */
    public void setDeviceBean(EncapDeviceService encapDeviceService) {

        this.encapDeviceService = encapDeviceService;
    }

    /**
     * @return The {@link EncapDeviceService} of the session.
     */
    public EncapDeviceService getDeviceService() {

        return encapDeviceService;
    }

    public boolean isChallenged() {

        return encapDeviceService != null && encapDeviceService.isChallenged();
    }
}
