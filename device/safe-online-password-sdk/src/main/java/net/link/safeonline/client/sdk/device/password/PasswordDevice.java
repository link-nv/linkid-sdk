package net.link.safeonline.client.sdk.device.password;

public interface PasswordDevice {

    String NAME = "password";

    int MIN_LENGTH = 6;

    // attributes
    String ATTRIBUTE_LOGIN = "device.password.login";

    // WS-Authentication
    String WS_AUTH_LOGIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:password:ws:auth:login";
    String WS_AUTH_PASSWORD_ATTRIBUTE = "urn:net:lin-k:safe-online:password:ws:auth:password";
    String WS_AUTH_EMAIL_ATTRIBUTE    = "urn:net:lin-k:safe-online:password:ws:auth:email";
}
