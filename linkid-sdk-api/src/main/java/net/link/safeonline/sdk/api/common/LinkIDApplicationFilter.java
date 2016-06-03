/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.common;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 02/06/16
 * Time: 13:12
 */
public class LinkIDApplicationFilter implements Serializable {

    private final List<String> applications;

    public LinkIDApplicationFilter(final List<String> applications) {

        this.applications = applications;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDUserFilter{" +
               "applications=" + applications +
               '}';
    }

    // Accessors

    public List<String> getApplications() {

        return applications;
    }
}
