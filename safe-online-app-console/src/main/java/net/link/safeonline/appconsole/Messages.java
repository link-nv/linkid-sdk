/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import java.util.ResourceBundle;


public enum Messages {

    TITLE("title"),
    IDENTITY("identity"),
    LOCATION("location"),
    FILE("file"),
    SERVICES("services"),
    LOAD_IDENTITY("load_identity"),
    CREATE_P12("create_p12"),
    EXTRACT_CERT("extract_cert"),
    SET_LOCATION("set_location"),
    QUIT("quit"),
    BROWSE("browse"),
    CERTIFICATE("certificate"),
    PRIVATE_KEY("private_key"),
    KEYSTORE("keystore"),
    KEYSTORE_TYPE("keystore_type"),
    KEYSTORE_PW("keystore_pw"),
    KEYENTRY_PW("keyentry_pw"),
    ERROR_MISSING_FIELDS("error_missing_fields"),
    ERROR_SELECT_KEYSTORE("error_select_keystore"),
    ERROR_KEYSTORE_PW("error_keystore_pw"),
    ERROR_KEYIDENTITY_PW("error_keyidentity_pw"),
    ERROR_CREATE_P12("error_create_p12"),
    ERROR_OPEN_KEYSTORE("error_open_keystore"),
    CERT_DN("cert_dn"),
    ECHO("echo"),
    ATTRIB("attrib"),
    AUTH("auth"),
    ECHO_INPUT("echo_input"),
    ECHO_OUTPUT("echo_output"),
    CANCEL("cancel"),
    GET_ATTRIBUTES("get_attributes"),
    USER("user"),
    AUTH_USER("auth_user"),
    DEBUG("debug"),
    REFRESH("refresh"),
    CAPTURE("capture"),
    VIEW_INBOUND_SOAP("view_inbound_soap"),
    VIEW_OUTBOUND_SOAP("view_outbound_soap"),
    UTILS("utils"),
    DONE("done"),
    CONFIRM_ID("confirm_identity"),
    CERT_ISSUER_DN("cert_issuer_dn"),
    CERT_SUBJECT_DN("cert_subject_dn"),
    CERT_SIG_ALGO("cert_sig_algo"),
    KEY_ALGO("key_algo"),
    KEY_FORMAT("key_format"),
    DELETE("delete"),
    ADD_ATTRIBUTE("add_attribute"),
    ATTRIBUTE_NAME("attribute_name"),
    ATTRIBUTE_TYPE("attribute_type"),
    ATTRIBUTE_VALUE("attribute_value"),
    APPLICATION("application"),
    PROTOCOL("protocol");

    private static final String  MESSAGES_RESOURCE = "net.link.safeonline.appconsole.messages";

    private final ResourceBundle messages          = ResourceBundle.getBundle(MESSAGES_RESOURCE);

    private final String         key;


    Messages(String key) {

        this.key = key;
    }

    public String getMessage() {

        return this.messages.getString(this.key);
    }
}
