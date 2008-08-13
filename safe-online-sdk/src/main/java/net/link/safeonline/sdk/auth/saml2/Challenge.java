/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.io.Serializable;


/**
 * Data container for the challenge used in the authentication protocol.
 * 
 * @author fcorneli
 * 
 * @param <T>
 */
public class Challenge<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T                 value;


    public void setValue(T value) {

        if (null != this.value) {
            throw new IllegalStateException("cannot set challenge value twice");
        }
        if (null == value) {
            throw new IllegalArgumentException("challenge value cannot be null");
        }
        this.value = value;
    }

    public T getValue() {

        if (null == this.value) {
            throw new IllegalStateException("challenge value was not set");
        }
        return this.value;
    }
}
