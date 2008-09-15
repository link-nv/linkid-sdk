/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment;

import java.io.Serializable;


public class CustomerStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            userId;

    private boolean           junior;


    public boolean isJunior() {

        return this.junior;
    }

    public CustomerStatus() {

        this(null, false);
    }

    public void setJunior(boolean junior) {

        this.junior = junior;
    }

    public CustomerStatus(final String userId, final boolean junior) {

        super();
        this.userId = userId;
        this.junior = junior;
    }

    public String getUserId() {

        return this.userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }
}
