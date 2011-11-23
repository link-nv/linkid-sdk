package net.link.safeonline.sdk.auth.protocol.openid;

import java.util.*;
import net.link.safeonline.attribute.AttributeSDK;
import net.link.safeonline.attribute.Compound;
import org.openid4java.message.ax.FetchResponse;


public abstract class OpenIdUtil {

    public static final String COMPOUND_TYPE_ID     = "E7AC5C06-077F-420B-A8C3-C8561A152330";
    public static final String MULTIVALUED_TYPE_ID  = "5870B6F9-CD4A-4496-854D-9A37EF9340AF";
    public static final String SINGLEVALUED_TYPE_ID = "272C6ADA-BA72-45FA-A9F9-E795CF70047C";

    public static final String TYPE_URI_ATTRIBUTE_ID           = "uri.attributeId.";
    public static final String TYPE_URI_ATTRIBUTE_TYPE         = "uri.type.";
    public static final String TYPE_URI_ATTRIBUTE_NAME         = "uri.name.";
    public static final String TYPE_URI_ATTRIBUTE_VALUE        = "uri.value.";
    public static final String TYPE_URI_MEMBER_ATTRIBTUE_NAME  = "uri.member.name.";
    public static final String TYPE_URI_MEMBER_ATTRIBTUE_VALUE = "uri.member.value.";

    /**
     * Parse LinkID attributes from OpenID {@link FetchResponse}
     *
     * @param fetchResponse OpenID fetch response
     *
     * @return map of LinkID attributes
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<AttributeSDK<?>>> getAttributeMap(final FetchResponse fetchResponse) {

        Map<String, List<AttributeSDK<?>>> attributeMap = new HashMap<String, List<AttributeSDK<?>>>();

        int idx = 0;
        while (true) {

            // get attribute ID
            String attributeIdAlias = fetchResponse.getAttributeAlias( TYPE_URI_ATTRIBUTE_ID + idx );
            if (null == attributeIdAlias)
                break;
            String attributeId = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_ID + idx );

            // get type
            String type = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_TYPE + attributeId );

            // get attribute name
            String attributeName = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_NAME + attributeId );

            // get attribute value
            if (type.equals( COMPOUND_TYPE_ID )) {

                // get members
                List<AttributeSDK<?>> members = new LinkedList<AttributeSDK<?>>();
                int memberIdx = 0;
                while (true) {
                    String memberNameAlias = fetchResponse.getAttributeAlias(
                            TYPE_URI_MEMBER_ATTRIBTUE_NAME + attributeId + '.' + memberIdx );
                    if (null == memberNameAlias)
                        break;

                    String memberName = getValue( fetchResponse, TYPE_URI_MEMBER_ATTRIBTUE_NAME + attributeId + '.' + memberIdx );
                    String memberValue = getValue( fetchResponse, TYPE_URI_MEMBER_ATTRIBTUE_VALUE + attributeId + '.' + memberIdx );

                    members.add( new AttributeSDK( attributeId, memberName, memberValue ) );

                    memberIdx++;
                }

                addToMap( attributeMap, new AttributeSDK( attributeId, attributeName, new Compound( members ) ) );
            } else {
                addToMap( attributeMap,
                        new AttributeSDK( attributeId, attributeName, getValue( fetchResponse, TYPE_URI_ATTRIBUTE_VALUE + attributeId ) ) );
            }

            idx++;
        }

        return attributeMap;
    }

    private static void addToMap(Map<String, List<AttributeSDK<?>>> attributeMap, AttributeSDK<?> attribute) {

        List<AttributeSDK<?>> attributes = attributeMap.get( attribute.getName() );
        if (null == attributes) {
            attributes = new LinkedList<AttributeSDK<?>>();
        }
        attributes.add( attribute );
        attributeMap.put( attribute.getName(), attributes );
    }

    private static String getValue(FetchResponse fetchResponse, String typeUri) {

        String alias = fetchResponse.getAttributeAlias( typeUri );
        if (null == alias)
            throw new RuntimeException( "Alias not found for typeUri: " + typeUri );

        String value = fetchResponse.getAttributeValue( alias );
        if (null == value)
            throw new RuntimeException( "Value not found for alias: " + alias + " typeUri: " + typeUri );
        return value;
    }
}
