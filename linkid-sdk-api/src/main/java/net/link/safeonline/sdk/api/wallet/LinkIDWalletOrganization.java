/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
public class LinkIDWalletOrganization implements Serializable {

    private final String                        id;
    private final String                        logoUrl;
    private final long                          expirationInSecs;
    private final boolean                       sticky;
    private final boolean                       autoEnroll;
    private final List<LinkIDLocalizationValue> nameLocalizations;
    private final List<LinkIDLocalizationValue> descriptionLocalizations;

    public LinkIDWalletOrganization(final String id, final String logoUrl, final long expirationInSecs, final boolean sticky, final boolean autoEnroll,
                                    final List<LinkIDLocalizationValue> nameLocalizations, final List<LinkIDLocalizationValue> descriptionLocalizations) {

        this.id = id;
        this.logoUrl = logoUrl;
        this.expirationInSecs = expirationInSecs;
        this.sticky = sticky;
        this.autoEnroll = autoEnroll;
        this.nameLocalizations = nameLocalizations;
        this.descriptionLocalizations = descriptionLocalizations;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletOrganization{" +
               "id='" + id + '\'' +
               ", logoUrl='" + logoUrl + '\'' +
               ", expirationInSecs=" + expirationInSecs +
               ", sticky=" + sticky +
               ", autoEnroll=" + autoEnroll +
               ", nameLocalizations=" + nameLocalizations +
               ", descriptionLocalizations=" + descriptionLocalizations +
               '}';
    }

    // Helper methods

    public String getId() {

        return id;
    }

    public String getLogoUrl() {

        return logoUrl;
    }

    public long getExpirationInSecs() {

        return expirationInSecs;
    }

    public boolean isSticky() {

        return sticky;
    }

    public boolean isAutoEnroll() {

        return autoEnroll;
    }

    public List<LinkIDLocalizationValue> getNameLocalizations() {

        return nameLocalizations;
    }

    public List<LinkIDLocalizationValue> getDescriptionLocalizations() {

        return descriptionLocalizations;
    }
}
