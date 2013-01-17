package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDSubscription implements Serializable {

    private final long    id;
    private final String  friendlyName;
    private final String  encodedDescription;
    private final String  url;
    private final boolean canUnsubscribe;

    private final Date lastLogin;
    private final int  authentications;

    private final List<LinkIDAttribute> identity;
    private final Date                  lastIdentityConfirmation;

    private final String encodedUA;
    private final Date   lastUsageConfirmation;

    public LinkIDSubscription(final long id, final String friendlyName, final String encodedDescription, final String url,
                              final boolean canUnsubscribe, final Date lastLogin, final int authentications, final List<LinkIDAttribute> identity,
                              final Date lastIdentityConfirmation, final String encodedUA, final Date lastUsageConfirmation) {

        this.id = id;
        this.friendlyName = friendlyName;
        this.encodedDescription = encodedDescription;
        this.url = url;
        this.canUnsubscribe = canUnsubscribe;

        this.lastLogin = lastLogin;
        this.authentications = authentications;

        this.identity = identity;
        this.lastIdentityConfirmation = lastIdentityConfirmation;

        this.encodedUA = encodedUA;
        this.lastUsageConfirmation = lastUsageConfirmation;
    }

    public long getId() {

        return id;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getEncodedDescription() {

        return encodedDescription;
    }

    public String getUrl() {

        return url;
    }

    public boolean isCanUnsubscribe() {

        return canUnsubscribe;
    }

    public Date getLastLogin() {

        return lastLogin;
    }

    public int getAuthentications() {

        return authentications;
    }

    public List<LinkIDAttribute> getIdentity() {

        return identity;
    }

    public Date getLastIdentityConfirmation() {

        return lastIdentityConfirmation;
    }

    public String getEncodedUA() {

        return encodedUA;
    }

    public Date getLastUsageConfirmation() {

        return lastUsageConfirmation;
    }
}
