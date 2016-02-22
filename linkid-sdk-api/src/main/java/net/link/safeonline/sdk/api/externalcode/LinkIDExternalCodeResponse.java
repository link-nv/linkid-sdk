package net.link.safeonline.sdk.api.externalcode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.link.util.InternalInconsistencyException;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 19/01/15
 * Time: 15:35
 */
public class LinkIDExternalCodeResponse implements Serializable {

    public static final String REFERENCE_KEY = "ExternalCodeResponseDO.reference";
    public static final String TYPE_KEY      = "ExternalCodeResponseDO.type";

    private final String                 reference;
    private final LinkIDExternalCodeType type;

    public LinkIDExternalCodeResponse(final String reference, final LinkIDExternalCodeType type) {

        this.reference = reference;
        this.type = type;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDExternalCodeResponse{" +
               "reference='" + reference + '\'' +
               ", type=" + type +
               '}';
    }

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<>();

        map.put( REFERENCE_KEY, reference );
        map.put( TYPE_KEY, type.name() );

        return map;
    }

    @Nullable
    public static LinkIDExternalCodeResponse fromMap(final Map<String, String> externalCodeResponseMap) {

        // check map valid
        if (!externalCodeResponseMap.containsKey( REFERENCE_KEY ))
            throw new InternalInconsistencyException( "External code response's reference field is not present!" );
        if (!externalCodeResponseMap.containsKey( TYPE_KEY ))
            throw new InternalInconsistencyException( "External code response's type field is not present!" );

        // convert
        return new LinkIDExternalCodeResponse( externalCodeResponseMap.get( REFERENCE_KEY ),
                LinkIDExternalCodeType.parse( externalCodeResponseMap.get( TYPE_KEY ) ) );
    }

    // Accessors

    public String getReference() {

        return reference;
    }

    public LinkIDExternalCodeType getType() {

        return type;
    }
}
