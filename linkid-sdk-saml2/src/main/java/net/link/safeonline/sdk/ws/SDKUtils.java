/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.lin_k.safe_online.common.Currency;
import net.lin_k.safe_online.common.PaymentStatusType;
import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.util.InternalInconsistencyException;


public abstract class SDKUtils {

    public static String getSDKProperty(final String key) {

        ResourceBundle properties = ResourceBundle.getBundle( "sdk_config" );
        return properties.getString( key );
    }

    public static XMLGregorianCalendar convert(final Date date) {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime( date );
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
        }
        catch (DatatypeConfigurationException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    public static net.link.safeonline.sdk.api.payment.Currency convert(final Currency currency) {

        switch (currency) {

            case EUR:
                return net.link.safeonline.sdk.api.payment.Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static Currency convert(final net.link.safeonline.sdk.api.payment.Currency currency) {

        switch (currency) {

            case EUR:
                return Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static PaymentState convert(final PaymentStatusType paymentState) {

        switch (paymentState) {

            case STARTED:
                return PaymentState.STARTED;
            case AUTHORIZED:
                return PaymentState.PAYED;
            case FAILED:
                return PaymentState.FAILED;
            case REFUNDED:
                return PaymentState.REFUNDED;
            case REFUND_STARTED:
                return PaymentState.REFUND_STARTED;
            case DEFERRED:
                return PaymentState.DEFERRED;
            case WAITING_FOR_UPDATE:
                return PaymentState.WAITING_FOR_UPDATE;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported payment state: \"%s\"", paymentState.name() ) );
    }
}
