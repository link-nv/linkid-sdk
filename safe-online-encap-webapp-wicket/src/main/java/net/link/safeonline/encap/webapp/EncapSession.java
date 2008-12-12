/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.encap.webapp;

import org.apache.wicket.Request;
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

    private static final long serialVersionUID = 1L;


    public EncapSession(Request request) {

        super(request);
    }
}
