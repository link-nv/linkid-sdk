/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.localization;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 15:07
 */
public class LinkIDLocalizationValue implements Serializable {

    private final String languageCode;
    private final String value;

    public LinkIDLocalizationValue(final String languageCode, final String value) {

        this.languageCode = languageCode;
        this.value = value;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLocalization{" +
               "languageCode='" + languageCode + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

    // Accessors

    public String getLanguageCode() {

        return languageCode;
    }

    public String getValue() {

        return value;
    }
}
