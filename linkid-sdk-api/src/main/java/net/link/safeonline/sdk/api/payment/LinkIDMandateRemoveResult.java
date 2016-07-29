/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 29/07/16
 * Time: 10:07
 */
@SuppressWarnings("unused")
public class LinkIDMandateRemoveResult implements Serializable {

    private final List<String> removedReferences;
    private final List<String> notFoundReferences;
    private final List<String> alreadyArchivedReferences;

    public LinkIDMandateRemoveResult(final List<String> removedReferences, final List<String> notFoundReferences,
                                     final List<String> alreadyArchivedReferences) {

        this.removedReferences = removedReferences;
        this.notFoundReferences = notFoundReferences;
        this.alreadyArchivedReferences = alreadyArchivedReferences;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this )
                          .add( "removedReferences", removedReferences )
                          .add( "notFoundReferences", notFoundReferences )
                          .add( "alreadyArchivedReferences", alreadyArchivedReferences )
                          .toString();
    }

    // Accessors

    public List<String> getRemovedReferences() {

        return removedReferences;
    }

    public List<String> getNotFoundReferences() {

        return notFoundReferences;
    }

    public List<String> getAlreadyArchivedReferences() {

        return alreadyArchivedReferences;
    }
}
