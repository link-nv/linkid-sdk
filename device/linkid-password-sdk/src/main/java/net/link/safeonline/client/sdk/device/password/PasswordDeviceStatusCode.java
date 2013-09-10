package net.link.safeonline.client.sdk.device.password;

import java.util.HashMap;
import java.util.Map;


public enum PasswordDeviceStatusCode {

    ILLEGAL_PASSWORD( "status.code.device.password.illegal.password" ),
    ILLEGAL_EMAIL( "status.code.device.password.illegal.email" );

    private final String code;

    private static final Map<String, PasswordDeviceStatusCode> codeMap = new HashMap<String, PasswordDeviceStatusCode>();

    static {
        PasswordDeviceStatusCode[] statusCodes = PasswordDeviceStatusCode.values();
        for (PasswordDeviceStatusCode statusCode : statusCodes)
            codeMap.put( statusCode.getCode(), statusCode );
    }

    PasswordDeviceStatusCode(String code) {

        this.code = code;
    }

    public String getCode() {

        return code;
    }

    @Override
    public String toString() {

        return code;
    }

    public static PasswordDeviceStatusCode ofCode(String code) {

        PasswordDeviceStatusCode authenticationStatusCode = codeMap.get( code );
        if (null == authenticationStatusCode)
            throw new IllegalArgumentException( "unknown Password device status code: " + code );
        return authenticationStatusCode;
    }

}
