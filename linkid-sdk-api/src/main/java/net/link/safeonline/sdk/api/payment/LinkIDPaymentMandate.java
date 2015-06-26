package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 26/06/15
 * Time: 08:21
 */
public class LinkIDPaymentMandate implements Serializable {

    private final String description;
    @Nullable
    private final String reference;

    public LinkIDPaymentMandate(final String description, @Nullable final String reference) {

        this.description = description;
        this.reference = reference;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentMandate{" +
               "description='" + description + '\'' +
               ", reference='" + reference + '\'' +
               '}';
    }

    // Accessors

    public String getDescription() {

        return description;
    }

    @Nullable
    public String getReference() {

        return reference;
    }
}
