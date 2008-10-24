/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Class that manages the authentication messages.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationMessages {

    public static enum KEY {
        START("start"),
        ERROR("error"),
        NO_BEID("noBeID"),
        DONE("done"),
        NO_READER("noReader"),
        NO_CARD("noCard"),
        SENDING("sending"),
        PERMISSION_DENIED("permissionDenied"),
        NOT_SUBSCRIBED("notSubscribed"),
        EID_NOT_REGISTERED("eidNotRegistered"),
        ENTER_PIN("enterPin");

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
    public AuthenticationMessages(Locale locale) {

        this.messages = ResourceBundle.getBundle("net.link.safeonline.auth.pcsc.AuthenticationMessages", locale);
    }

    /**
     * Gives back the message for the given key.
     */
    public String getString(KEY key) {

        return this.messages.getString(key.getKey());
    }
}
