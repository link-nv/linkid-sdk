package net.link.safeonline.sdk.api.externalcode;

/**
 * Created by wvdhaute
 * Date: 15/01/14
 * Time: 11:49
 */
public enum LinkIDExternalCodeType {

    PARKO,
    PARKO_TEST,
    LTQR;

    public static LinkIDExternalCodeType parse(final String externalCodeTypeString) {

        if (null == externalCodeTypeString || 0 == externalCodeTypeString.trim().length())
            return PARKO;

        for (LinkIDExternalCodeType linkIDExternalCodeType : LinkIDExternalCodeType.values()) {
            if (linkIDExternalCodeType.name().equals( externalCodeTypeString )) {
                return linkIDExternalCodeType;
            }
        }

        throw new RuntimeException( String.format( "Unsupported external code type: \"%s\"", externalCodeTypeString ) );
    }
}
