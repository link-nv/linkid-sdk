/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import org.opensaml.saml2.core.StatusCode;


/**
 * <h2>{@link LinkIDDeviceStatus}</h2>
 * <p/>
 * <p>
 * <i>Feb 26, 2010</i>
 * </p>
 *
 * @author wvdhaute
 */
public enum LinkIDDeviceStatus {

    SUCCESS( StatusCode.SUCCESS_URI ),
    FAIL( StatusCode.AUTHN_FAILED_URI ),
    EXPIRED( StatusCode.AUTHN_FAILED_URI, "urn:net:lin-k:safe-online:authentication:status:expired" );

    private final String statusCode;
    private       String secondLevelStatusCode;

    LinkIDDeviceStatus(String statusCode, String secondLevelStatusCode) {

        this.statusCode = statusCode;
        this.secondLevelStatusCode = secondLevelStatusCode;
    }

    LinkIDDeviceStatus(String statusCode) {

        this.statusCode = statusCode;
        secondLevelStatusCode = null;
    }

    public String getStatusCode() {

        return statusCode;
    }

    public String getSecondLevelStatusCode() {

        return secondLevelStatusCode;
    }

    public void setSecondLevelStatusCode(String secondLevelStatusCode) {

        this.secondLevelStatusCode = secondLevelStatusCode;
    }

    @Override
    public String toString() {

        return String.format( "%s - %s", statusCode, secondLevelStatusCode );
    }

    public static LinkIDDeviceStatus getStatus(String statusCode, String secondLevelStatusCode)
            throws LinkIDUnknownStatusException {

        for (LinkIDDeviceStatus status : LinkIDDeviceStatus.values())
            if (status.getStatusCode().equals( statusCode )) {
                if (null != status.getSecondLevelStatusCode() && status.getSecondLevelStatusCode().equals( secondLevelStatusCode )
                    || null == status.getSecondLevelStatusCode())
                    return status;
            }

        throw new LinkIDUnknownStatusException( statusCode );
    }
}
