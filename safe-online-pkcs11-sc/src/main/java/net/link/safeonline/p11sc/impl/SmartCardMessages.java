/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.impl;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Class that manages the smart card messages.
 * 
 * @author fcorneli
 */
public class SmartCardMessages {

    public static enum KEY {
        NO_CARD("noCard");

        private final String key;


        private KEY(String key) {

            this.key = key;
        }

        public String getKey() {

            return this.key;
        }
    }


    private ResourceBundle messages;


    /**
     * Main constructor.
     * 
     * @param locale
     */
    public SmartCardMessages(Locale locale) {

        this.messages = ResourceBundle.getBundle("net.link.safeonline.pkcs11.SmartCardMessages", locale);
    }

    /**
     * Gives back the message for the given key.
     */
    public String getString(KEY key) {

        return this.messages.getString(key.getKey());
    }
}
