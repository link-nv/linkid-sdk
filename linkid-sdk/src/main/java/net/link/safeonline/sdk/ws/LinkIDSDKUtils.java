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
import net.lin_k.safe_online.common.PaymentMethodType;
import net.lin_k.safe_online.common.PaymentStatusType;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.util.InternalInconsistencyException;


public abstract class LinkIDSDKUtils {

    public static String getSDKProperty(final String key) {

        ResourceBundle properties = ResourceBundle.getBundle( "sdk_config" );
        return properties.getString( key );
    }

    public static XMLGregorianCalendar convert(final Date date) {

        if (null == date)
            return null;

        GregorianCalendar c = new GregorianCalendar();
        c.setTime( date );
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
        }
        catch (DatatypeConfigurationException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    public static Date convert(final XMLGregorianCalendar calender) {

        if (null == calender)
            return null;

        return calender.toGregorianCalendar().getTime();
    }

    public static LinkIDCurrency convert(final Currency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return LinkIDCurrency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static Currency convert(final LinkIDCurrency currency) {

        if (null == currency)
            return null;

        switch (currency) {

            case EUR:
                return Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported currency: \"%s\"", currency.name() ) );
    }

    public static LinkIDPaymentState convert(final PaymentStatusType paymentState) {

        if (null == paymentState)
            return null;

        switch (paymentState) {

            case STARTED:
                return LinkIDPaymentState.STARTED;
            case AUTHORIZED:
                return LinkIDPaymentState.PAYED;
            case FAILED:
                return LinkIDPaymentState.FAILED;
            case REFUNDED:
                return LinkIDPaymentState.REFUNDED;
            case REFUND_STARTED:
                return LinkIDPaymentState.REFUND_STARTED;
            case WAITING_FOR_UPDATE:
                return LinkIDPaymentState.WAITING_FOR_UPDATE;
        }

        throw new InternalInconsistencyException( String.format( "Unsupported payment state: \"%s\"", paymentState.name() ) );
    }

    public static PaymentMethodType convert(final LinkIDPaymentMethodType linkIDPaymentMethodType) {

        if (null == linkIDPaymentMethodType)
            return null;

        switch (linkIDPaymentMethodType) {

            case UNKNOWN:
                return PaymentMethodType.UNKNOWN;
            case VISA:
                return PaymentMethodType.VISA;
            case MASTERCARD:
                return PaymentMethodType.MASTERCARD;
            case SEPA:
                return PaymentMethodType.SEPA;
            case KLARNA:
                return PaymentMethodType.KLARNA;
        }

        return PaymentMethodType.UNKNOWN;
    }

    public static LinkIDPaymentMethodType convert(final PaymentMethodType paymentMethodType) {

        if (null == paymentMethodType)
            return null;

        switch (paymentMethodType) {

            case UNKNOWN:
                return LinkIDPaymentMethodType.UNKNOWN;
            case VISA:
                return LinkIDPaymentMethodType.VISA;
            case MASTERCARD:
                return LinkIDPaymentMethodType.MASTERCARD;
            case SEPA:
                return LinkIDPaymentMethodType.SEPA;
            case KLARNA:
                return LinkIDPaymentMethodType.KLARNA;
        }

        return LinkIDPaymentMethodType.UNKNOWN;
    }
}
