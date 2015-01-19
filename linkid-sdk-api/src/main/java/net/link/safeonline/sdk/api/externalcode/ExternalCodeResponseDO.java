package net.link.safeonline.sdk.api.externalcode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 19/01/15
 * Time: 15:35
 */
public class ExternalCodeResponseDO implements Serializable {

    public static final String REFERENCE_KEY = "ExternalCodeResponseDO.reference";
    public static final String TYPE_KEY      = "ExternalCodeResponseDO.type";

    private final String           reference;
    private final ExternalCodeType type;

    public ExternalCodeResponseDO(final String reference, final ExternalCodeType type) {

        this.reference = reference;
        this.type = type;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( REFERENCE_KEY, reference );
        map.put( TYPE_KEY, type.name() );

        return map;
    }

    @Nullable
    public static ExternalCodeResponseDO fromMap(final Map<String, String> externalCodeResponseMap) {

        // check map valid
        if (!externalCodeResponseMap.containsKey( REFERENCE_KEY ))
            throw new RuntimeException( "External code response's reference field is not present!" );
        if (!externalCodeResponseMap.containsKey( TYPE_KEY ))
            throw new RuntimeException( "External code response's type field is not present!" );

        // convert
        return new ExternalCodeResponseDO( externalCodeResponseMap.get( REFERENCE_KEY ), ExternalCodeType.parse( externalCodeResponseMap.get( TYPE_KEY ) ) );
    }

    // Accessors

    public String getReference() {

        return reference;
    }

    public ExternalCodeType getType() {

        return type;
    }
}
