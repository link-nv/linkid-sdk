package net.link.safeonline.sdk.api.payment;

/**
 * Created by wvdhaute
 * Date: 19/09/14
 * Time: 15:52
 */
public enum LinkIDPaymentAddBrowser {

    NOT_ALLOWED,
    REDIRECT;

    public static LinkIDPaymentAddBrowser parse(final String valueString) {

        if (null == valueString)
            return LinkIDPaymentAddBrowser.NOT_ALLOWED;

        for (LinkIDPaymentAddBrowser value : LinkIDPaymentAddBrowser.values()) {

            if (value.name().equals( valueString.toUpperCase() )) {
                return value;
            }
        }

        throw new RuntimeException( String.format( "Unsupported PaymentAddBrowser option %s!", valueString ) );
    }

}
