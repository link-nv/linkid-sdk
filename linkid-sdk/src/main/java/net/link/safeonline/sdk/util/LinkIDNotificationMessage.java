package net.link.safeonline.sdk.util;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationConstants;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationTopic;


/**
 * Created by wvdhaute
 * Date: 15/10/15
 * Time: 11:24
 */
@SuppressWarnings("unused")
public class LinkIDNotificationMessage implements Serializable {

    private final LinkIDNotificationTopic topic;
    //
    // Core notifications
    private final String                  userId;
    private final String                  filter;
    private final String                  info;
    //
    // Payment status
    private final String                  paymentOrderReference;
    //
    // LTQR
    private final String                  ltqrReference;
    private final String                  ltqrClientSessionId;
    private final String                  ltqrPaymentOrderReference;
    //
    // Theme
    private final String                  themeName;

    public LinkIDNotificationMessage(final HttpServletRequest request) {

        this.topic = LinkIDNotificationTopic.to( request.getParameter( LinkIDNotificationConstants.TOPIC_PARAM ) );
        this.userId = request.getParameter( LinkIDNotificationConstants.USER_ID_PARAM );
        this.filter = request.getParameter( LinkIDNotificationConstants.FILTER_PARAM );
        this.info = request.getParameter( LinkIDNotificationConstants.INFO_PARAM );
        this.paymentOrderReference = request.getParameter( LinkIDNotificationConstants.PAYMENT_ORDER_REF_PARAM );
        this.ltqrReference = request.getParameter( LinkIDNotificationConstants.LTQR_REF_PARAM );
        this.ltqrClientSessionId = request.getParameter( LinkIDNotificationConstants.LTQR_CLIENT_SESSION_ID_PARAM );
        this.ltqrPaymentOrderReference = request.getParameter( LinkIDNotificationConstants.LTQR_PAYMENT_ORDER_REF_PARAM );
        this.themeName = request.getParameter( LinkIDNotificationConstants.THEME_NAME_PARAM );
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDNotificationMessage{" +
               "topic=" + topic +
               ", userId='" + userId + '\'' +
               ", filter='" + filter + '\'' +
               ", info='" + info + '\'' +
               ", paymentOrderReference='" + paymentOrderReference + '\'' +
               ", ltqrReference='" + ltqrReference + '\'' +
               ", ltqrClientSessionId='" + ltqrClientSessionId + '\'' +
               ", ltqrPaymentOrderReference='" + ltqrPaymentOrderReference + '\'' +
               ", themeName='" + themeName + '\'' +
               '}';
    }

    // Accessors

    public LinkIDNotificationTopic getTopic() {

        return topic;
    }

    public String getUserId() {

        return userId;
    }

    public String getFilter() {

        return filter;
    }

    public String getInfo() {

        return info;
    }

    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }

    public String getLtqrReference() {

        return ltqrReference;
    }

    public String getLtqrClientSessionId() {

        return ltqrClientSessionId;
    }

    public String getLtqrPaymentOrderReference() {

        return ltqrPaymentOrderReference;
    }

    public String getThemeName() {

        return themeName;
    }
}
