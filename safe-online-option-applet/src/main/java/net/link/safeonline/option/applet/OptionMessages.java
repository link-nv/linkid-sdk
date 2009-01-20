/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.applet;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Class that manages the authentication messages.
 * 
 * @author fcorneli
 * 
 */
public class OptionMessages {

    public static enum KEY {
        START("start"),
        ERROR("error"),
        NO_DATACARD("noDatacard"),
        DONE("done"),
        SENDING("sending"),
        PIN("pin"),
        PERMISSION_DENIED("permissionDenied"),
        NOT_SUBSCRIBED("notSubscribed"),
        DATACARD_NOT_REGISTERED("datacardNotRegistered"),
        ENTER_PIN("enterPin"),
        DATACARD_DISABLED("datacardDisabled");

        private final String key;


        private KEY(String key) {

            this.key = key;
        }

        public String getKey() {

            return key;
        }
    }


    private ResourceBundle messages;


    /**
     * Main constructor.
     * 
     * @param locale
     */
    public OptionMessages(Locale locale) {

        messages = ResourceBundle.getBundle("net.link.safeonline.option.applet.OptionMessages", locale);
    }

    /**
     * Gives back the message for the given key.
     */
    public String getString(KEY key) {

        return messages.getString(key.getKey());
    }
}
