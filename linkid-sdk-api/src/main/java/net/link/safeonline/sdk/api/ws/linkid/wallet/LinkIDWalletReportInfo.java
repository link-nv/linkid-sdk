package net.link.safeonline.sdk.api.ws.linkid.wallet;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 05/01/16
 * Time: 13:31
 */
public class LinkIDWalletReportInfo implements Serializable {

    @Nullable
    private final String reference;
    @Nullable
    private final String description;

    public LinkIDWalletReportInfo(@Nullable final String reference, @Nullable final String description) {

        this.reference = reference;
        this.description = description;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletReportInfo{" +
               "reference='" + reference + '\'' +
               ", description='" + description + '\'' +
               '}';
    }

    // Accessors

    @Nullable
    public String getReference() {

        return reference;
    }

    @Nullable
    public String getDescription() {

        return description;
    }
}
