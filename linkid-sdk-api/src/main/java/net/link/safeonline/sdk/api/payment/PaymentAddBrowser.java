package net.link.safeonline.sdk.api.payment;

/**
 * Created by wvdhaute
 * Date: 19/09/14
 * Time: 15:52
 */
public enum PaymentAddBrowser {

    NOT_ALLOWED,
    POPUP,
    REDIRECT;

    public static PaymentAddBrowser parse(final String valueString) {

        if (null == valueString)
            return PaymentAddBrowser.NOT_ALLOWED;

        for (PaymentAddBrowser value : PaymentAddBrowser.values()) {

            if (value.name().equals( valueString.toUpperCase() )) {
                return value;
            }
        }

        throw new RuntimeException( String.format( "Unsupported PaymentAddBrowser option %s!", valueString ) );
    }

}
