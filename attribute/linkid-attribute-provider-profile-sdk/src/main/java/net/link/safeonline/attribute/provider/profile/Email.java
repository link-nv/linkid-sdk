package net.link.safeonline.attribute.provider.profile;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.attribute.Compound;


@SuppressWarnings("UnusedDeclaration")
public class Email implements Serializable {

    private final String  email;
    private final boolean confirmed;

    public Email(final String email, final boolean confirmed) {

        this.email = email;
        this.confirmed = confirmed;
    }

    // Helper methods

    public static List<Email> getEmails(final List<AttributeSDK<Serializable>> emailAttributes) {

        if (null == emailAttributes)
            return new LinkedList<Email>();

        List<Email> emails = new LinkedList<Email>();
        for (AttributeSDK<Serializable> emailAttribute : emailAttributes) {

            Compound emailCompound = (Compound) emailAttribute.getValue();
            boolean confirmed = false;
            if (null != emailCompound.findMember( ProfileConstants.EMAIL_CONFIRMED ))
                confirmed = (Boolean) emailCompound.getMember( ProfileConstants.EMAIL_CONFIRMED ).getValue();
            Email email = new Email( (String) emailCompound.getMember( ProfileConstants.EMAIL_ADDRESS ).getValue(), confirmed );
            emails.add( email );
        }

        return emails;
    }

    // Accessors

    public String getEmail() {

        return email;
    }

    public boolean isConfirmed() {

        return confirmed;
    }
}
