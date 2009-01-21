package net.link.safeonline.model.otpoversms;

import net.link.safeonline.Startable;


public interface OtpOverSmsConstants {

    public static final String OTPOVERSMS_STARTABLE_JNDI_PREFIX    = "SafeOnlineOtpOverSms/startup/";

    public static final String OTPOVERSMS_DEVICE_ID                = "OtpOverSms";

    public static final String OTPOVERSMS_IDENTIFIER_DOMAIN        = "otpoversms";

    // OTP over SMS device attributes
    public static final String OTPOVERSMS_MOBILE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:otpoversms:mobile";
    public static final String OTPOVERSMS_PIN_HASH_ATTRIBUTE       = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:hash";
    public static final String OTPOVERSMS_PIN_SEED_ATTRIBUTE       = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:seed";
    public static final String OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE  = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:algorithm";
    public static final String OTPOVERSMS_PIN_ATTEMPTS_ATTRIBUTE   = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:attempts";
    public static final String OTPOVERSMS_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:otpoversms:device";
    public static final String OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:otpoversms:device:disable";

    // OTP over SMS WS authentication device credential attributes
    public static final String OTPOVERSMS_WS_AUTH_MOBILE_ATTRIBUTE = "urn:net:lin-k:safe-online:otpoversms:ws:auth:mobile";
    public static final String OTPOVERSMS_WS_AUTH_OTP_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:otp";
    public static final String OTPOVERSMS_WS_AUTH_PIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:pin";

    public static final int    OTPOVERSMS_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;
}
