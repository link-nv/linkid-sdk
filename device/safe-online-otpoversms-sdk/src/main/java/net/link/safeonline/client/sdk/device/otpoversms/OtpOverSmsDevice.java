package net.link.safeonline.client.sdk.device.otpoversms;

public interface OtpOverSmsDevice {

    String NAME = "otpoversms";

    // attributes
    String ATTRIBUTE_DEVICE            = "device.otpoversms";
    String ATTRIBUTE_MOBILE            = "device.otpoversms.mobile";
    String ATTRIBUTE_PIN_HASH          = "device.otpoversms.pin.hash";
    String ATTRIBUTE_PIN_SEED          = "device.otpoversms.pin.seed";
    String ATTRIBUTE_PIN_ALGORITHM     = "device.otpoversms.pin.algorithm";
    String ATTRIBUTE_PIN_NEW_ALGORITHM = "device.otpoversms.pin.algorithm.new";
    String ATTRIBUTE_PIN_ATTEMPTS      = "device.otpoversms.pin.attempts";
    String ATTRIBUTE_DEVICE_DISABLE    = "device.otpoversms.disable";

    // WS-Authentication
    String WS_AUTH_MOBILE_ATTRIBUTE = "urn:net:lin-k:safe-online:otpoversms:ws:auth:mobile";
    String WS_AUTH_OTP_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:otp";
    String WS_AUTH_PIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:pin";
}
