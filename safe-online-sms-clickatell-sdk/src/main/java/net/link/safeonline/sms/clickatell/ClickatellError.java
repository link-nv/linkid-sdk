/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * <h2>{@link ClickatellError}<br>
 * <sub>Clickatell error codes</sub></h2>
 * 
 * <p>
 * Lists all documented clickatell error codes.
 * </p>
 * 
 * <p>
 * <i>Feb 20, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public enum ClickatellError {

    AUTH_FAIL("001", "authentication failed"),
    UNKNOWN_U_P("002", "unknown username or password"),
    SESSIONID_EXPIRED("003", "session expired"),
    ACCOUNT_FROZEN("004", "account frozen"),
    SESSIONID_MISSING("005", "session id missing"),
    IP_VIOLATION("007", "IP lockdown violation"),
    INVALID_PARAMS("101", "invalid or missing parameters"),
    INVALID_UDH("102", "invalid user data header"),
    UNKNOWN_API_MSG_ID("103", "unknown API message ID"),
    UNKNOWN_CLIENT_MSG_ID("104", "unknown client message ID"),
    INVALID_DEST("105", "invalid destination address"),
    INVALID_SRC("106", "invalid source address"),
    EMPTY_MSG("107", "emtpy message"),
    INVALID_API_ID("108", "invalid or missing API ID"),
    MISSING_MSG_ID("109", "missing message id"),
    ERROR_MAIL("110", "error with email message"),
    INVALID_PROTO("111", "invalid protocol"),
    INVALID_MSG_TYPE("112", "invalid message type"),
    MAX_MSG_PARTS_EXCEEDED("113", "maximum message parts exceeded"),
    NO_ROUTE("114", "cannot route message"),
    MSG_EXPIRED("115", "message expired"),
    INVALID_UNICODE("116", "invalid unicode data"),
    INVALID_DELIVERY_TIME("120", "invalid delivery time"),
    DEST_BLOCKED("121", "destination mobile number blocked"),
    DEST_OPT_OUT("122", "destination mobile number opt out"),
    INVALID_SENDER_ID("123", "invalid sender id"),
    NUMBER_DELISTED("128", "number delisted"),
    INVALID_BATCH_ID("201", "invalid batch id"),
    NO_BATCH_TEMPLATE("202", "no batch template"),
    NO_CREDIT_LEFT("301", "no credit left"),
    MAX_ALLOWED_CREDIT("302", "max allowed credit");

    private String                                    code;
    private String                                    message;

    private static final Map<String, ClickatellError> lookup = new HashMap<String, ClickatellError>();

    static {
        for (ClickatellError e : EnumSet.allOf(ClickatellError.class)) {
            lookup.put(e.getCode(), e);
        }
    }


    ClickatellError(String code, String message) {

        this.code = code;
        this.message = message;
    }

    public String getCode() {

        return code;
    }

    public String getMessage() {

        return message;
    }

    public static ClickatellError get(String lookupcode) {

        return lookup.get(lookupcode);
    }
}
