package net.link.safeonline.sdk.api.ws.auth;

public enum LinkIDPaymentType {

    VISA,
    MC,
    MAESTRO,
    NEW,
    KLARNA;

    public static LinkIDPaymentType parse(final String paymentCodeTypeString) {

        if (null == paymentCodeTypeString || 0 == paymentCodeTypeString.trim().length())
            return null;

        for (LinkIDPaymentType paymentCodeType : LinkIDPaymentType.values()) {
            if (paymentCodeType.name().equals( paymentCodeTypeString )) {
                return paymentCodeType;
            }
        }

        return null;
    }
}
