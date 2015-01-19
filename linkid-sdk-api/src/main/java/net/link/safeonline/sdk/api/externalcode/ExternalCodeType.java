package net.link.safeonline.sdk.api.externalcode;

/**
 * Created by wvdhaute
 * Date: 15/01/14
 * Time: 11:49
 */
public enum ExternalCodeType {

    PARKO,
    PARKO_TEST,
    LTQR;

    public static ExternalCodeType parse(final String externalCodeTypeString) {

        if (null == externalCodeTypeString || 0 == externalCodeTypeString.trim().length())
            return PARKO;

        for (ExternalCodeType externalCodeType : ExternalCodeType.values()) {
            if (externalCodeType.name().equals( externalCodeTypeString )) {
                return externalCodeType;
            }
        }

        throw new RuntimeException( String.format( "Unsupported external code type: \"%s\"", externalCodeTypeString ) );
    }
}
