/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.auth.ws;

/**
 * <h2>{@link AuthenticationStep}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 18, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public enum AuthenticationStep {

    GLOBAL_USAGE_AGREEMENT("Global Usage Agreement"), USAGE_AGREEMENT(
            "Usage Agreement"), IDENTITY_CONFIRMATION("Identity Confirmation"), MISSING_ATTRIBUTES(
            "Missing Attributes");

    private final String value;


    private AuthenticationStep(String value) {

        this.value = value;
    }


    public String getValue() {

        return this.value;
    }

}
