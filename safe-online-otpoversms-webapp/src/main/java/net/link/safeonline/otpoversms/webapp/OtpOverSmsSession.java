/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import org.apache.wicket.Request;
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

    private static final long serialVersionUID = 1L;


    public OtpOverSmsSession(Request request) {

        super(request);
    }
}
