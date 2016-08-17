/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.attribute.LinkIDCompound;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDEmail implements Serializable {

    private final String  email;
    private final boolean confirmed;

    public LinkIDEmail(final String email, final boolean confirmed) {

        this.email = email;
        this.confirmed = confirmed;
    }

    // Helper methods

    public static List<LinkIDEmail> getEmails(final List<LinkIDAttribute<Serializable>> emailAttributes) {

        if (null == emailAttributes)
            return new LinkedList<>();

        List<LinkIDEmail> linkIDEmails = new LinkedList<>();
        for (LinkIDAttribute<Serializable> emailAttribute : emailAttributes) {

            LinkIDCompound emailLinkIDCompound = (LinkIDCompound) emailAttribute.getValue();
            boolean confirmed = false;
            if (null != emailLinkIDCompound.findMember( LinkIDProfileConstants.EMAIL_CONFIRMED ))
                confirmed = (Boolean) emailLinkIDCompound.getMember( LinkIDProfileConstants.EMAIL_CONFIRMED ).getValue();
            LinkIDEmail linkIDEmail = new LinkIDEmail( (String) emailLinkIDCompound.getMember( LinkIDProfileConstants.EMAIL_ADDRESS ).getValue(), confirmed );
            linkIDEmails.add( linkIDEmail );
        }

        return linkIDEmails;
    }

    // Accessors

    public String getEmail() {

        return email;
    }

    public boolean isConfirmed() {

        return confirmed;
    }
}
