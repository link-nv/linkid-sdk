/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription;

import java.io.Serializable;


public class UserStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            name;

    private String            userId;

    private boolean           admin;

    private boolean           careProvider;

    private boolean           pharmacist;


    public UserStatus() {

        this(null, null, false, false, false);
    }

    public UserStatus(String name, String userId, boolean admin, boolean careProvider, boolean pharmacist) {

        super();
        this.name = name;
        this.userId = userId;
        this.admin = admin;
        this.careProvider = careProvider;
        this.pharmacist = pharmacist;
    }

    public boolean isAdmin() {

        return this.admin;
    }

    public void setAdmin(boolean admin) {

        this.admin = admin;
    }

    public boolean isCareProvider() {

        return this.careProvider;
    }

    public void setCareProvider(boolean careProvider) {

        this.careProvider = careProvider;
    }

    public boolean isPharmacist() {

        return this.pharmacist;
    }

    public void setPharmacist(boolean pharmacist) {

        this.pharmacist = pharmacist;
    }

    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getUserId() {

        return this.userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }
}
