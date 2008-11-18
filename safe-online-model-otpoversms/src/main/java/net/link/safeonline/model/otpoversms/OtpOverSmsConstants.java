package net.link.safeonline.model.otpoversms;

import net.link.safeonline.Startable;


public class OtpOverSmsConstants {

    private OtpOverSmsConstants() {

        // empty
    }


    public static final String OTPOVERSMS_STARTABLE_JNDI_PREFIX    = "SafeOnlineOtpOverSms/startup/";

    public static final String OTPOVERSMS_DEVICE_ID                = "OtpOverSms";

    public static final String OTPOVERSMS_IDENTIFIER_DOMAIN        = "otpoversms";

    public static final String OTPOVERSMS_MOBILE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:otpoversms:mobile";

    public static final String OTPOVERSMS_PIN_HASH_ATTRIBUTE       = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:hash";

    public static final String OTPOVERSMS_PIN_SEED_ATTRIBUTE       = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:seed";

    public static final String OTPOVERSMS_PIN_ALGORITHM_ATTRIBUTE  = "urn:net:lin-k:safe-online:attribute:otpoversms:pin:algorithm";

    public static final String OTPOVERSMS_DEVICE_ATTRIBUTE         = "urn:net:lin-k:safe-online:attribute:device:otpoversms";

    public static final String OTPOVERSMS_DEVICE_DISABLE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:device:otpoversms:disable";

    public static final int    OTPOVERSMS_BOOT_PRIORITY            = Startable.PRIORITY_BOOTSTRAP - 1;
}
