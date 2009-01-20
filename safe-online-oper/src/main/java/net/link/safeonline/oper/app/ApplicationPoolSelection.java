/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app;

import java.io.Serializable;


public class ApplicationPoolSelection implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            name;

    private boolean           included;


    public ApplicationPoolSelection(String name, boolean included) {

        this.name = name;
        this.included = included;
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
}
