/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.custom.converter;

import java.io.Serializable;


/**
 * <h2>{@link PhoneNumber}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 2, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class PhoneNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            number;


    public PhoneNumber(String number) {

        this.number = number;
    }

    public String getNumber() {

        return this.number;
    }

    public void setNumber(String number) {

        this.number = number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return this.number;
    }
}
