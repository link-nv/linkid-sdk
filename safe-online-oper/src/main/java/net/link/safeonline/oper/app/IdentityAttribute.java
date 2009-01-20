/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import java.io.Serializable;


public class IdentityAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            name;

    private boolean           included;

    private boolean           required;

    private boolean           dataMining;


    public boolean isDataMining() {

        return dataMining;
    }

    public void setDataMining(boolean dataMining) {

        this.dataMining = dataMining;
    }

    public IdentityAttribute(String name) {

        this.name = name;
        included = false;
        required = false;
    }

    public IdentityAttribute(String name, boolean included, boolean required, boolean dataMining) {

        this.name = name;
        this.included = included;
        this.required = required;
        this.dataMining = dataMining;
    }

    public boolean isIncluded() {

        return included;
    }

    public void setIncluded(boolean included) {

        this.included = included;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }
}
