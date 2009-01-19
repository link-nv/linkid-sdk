/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import net.link.safeonline.ws.common.WSAuthenticationErrorCode;


/**
 * <h2>{@link AuthenticationError}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 19, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AuthenticationError {

    private WSAuthenticationErrorCode code;

    private String                    message;


    public AuthenticationError(WSAuthenticationErrorCode code, String message) {

        this.code = code;
        this.message = message;
    }

    public WSAuthenticationErrorCode getCode() {

        return this.code;
    }

    public String getMessage() {

        return this.message;
    }

}
