/*
 * SafeOnline project.
 *
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 ******************************************************************************/

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.Serializable;
import java.util.*;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.util.saml.Saml2Utils;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.Element;


/**
 * <h2>{@link LinkIDSaml2Utils}<br> <sub>Utility class for SAML.</sub></h2>
 * <p/>
 * <p> Utility class for SAML. </p>
 * <p/>
 * <p> <i>Dec 16, 2008</i> </p>
 *
 * @author wvdhaute
 */
public abstract class LinkIDSaml2Utils extends Saml2Utils {

    static final Logger logger = Logger.get( LinkIDSaml2Utils.class );

    public static <X extends XMLObject> X unmarshall(Element xmlElement) {
        // TODO: Is this really still necessary?

        X xmlObject = Saml2Utils.<X>unmarshall( xmlElement );
        xmlObject.addNamespace(
                new Namespace( WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE, WebServiceConstants.SAFE_ONLINE_SAML_PREFIX ) );

        return xmlObject;
    }

    public static Map<String, Object> getAttributeValues(final Response samlResponse) {

        ImmutableMap.Builder<String, Object> attributeValues = ImmutableMap.builder();
        for (Assertion assertion : samlResponse.getAssertions())
            attributeValues.putAll( getAttributeValues( assertion ) );

        return attributeValues.build();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<AttributeSDK<?>>> getAttributeValues(Assertion assertion) {

        Map<String, List<AttributeSDK<?>>> attributeMap = Maps.newHashMap();
        List<AttributeStatement> attrStatements = assertion.getAttributeStatements();
        if (attrStatements == null || attrStatements.isEmpty())
            return ImmutableMap.of();

        AttributeStatement attributeStatement = attrStatements.get( 0 );

        for (Attribute attribute : attributeStatement.getAttributes()) {

            AttributeSDK<?> attributeSDK = getAttributeSDK( attribute );

            List<AttributeSDK<?>> attributes = attributeMap.get( attributeSDK.getName() );
            if (null == attributes) {
                attributes = new LinkedList<AttributeSDK<?>>();
            }
            attributes.add( attributeSDK );
            attributeMap.put( attributeSDK.getName(), attributes );
        }

        return attributeMap;
    }

    private static AttributeSDK<?> getAttributeSDK(Attribute attributeType) {

        String attributeId = attributeType.getUnknownAttributes().get( WebServiceConstants.ATTRIBUTE_ID );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName(), null );

        List<XMLObject> attributeValues = attributeType.getAttributeValues();
        if (attributeValues.isEmpty())
            return attribute;

        XMLObject xmlValue = attributeValues.get( 0 );
        if (null != xmlValue.getOrderedChildren() && !xmlValue.getOrderedChildren().isEmpty()) {

            // compound
            List<AttributeSDK<?>> compoundMembers = new LinkedList<AttributeSDK<?>>();
            for (XMLObject memberAttributeObject : attributeValues.get( 0 ).getOrderedChildren()) {

                Attribute memberAttribute = (Attribute) memberAttributeObject;
                AttributeSDK<Serializable> member = new AttributeSDK<Serializable>( attributeId, memberAttribute.getName(), null );
                if (!memberAttribute.getAttributeValues().isEmpty()) {
                    member.setValue( toJavaObject( memberAttribute.getAttributeValues().get( 0 ) ) );
                }
                compoundMembers.add( member );
            }
            attribute.setValue( new Compound( compoundMembers ) );
        } else {
            // single/multi valued
            attribute.setValue( toJavaObject( xmlValue ) );
        }

        return attribute;
    }

    public static List<String> getAuthenticatedDevices(final Response samlResponse) {

        if (samlResponse.getAssertions().isEmpty())
            return new LinkedList<String>();

        return getAuthenticatedDevices( samlResponse.getAssertions().get( 0 ) );
    }

    public static List<String> getAuthenticatedDevices(final Assertion assertion) {

        List<String> authenticatedDevices = new LinkedList<String>();
        for (AuthnStatement authnStatement : assertion.getAuthnStatements()) {
            authenticatedDevices.add( authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
            logger.dbg( "authenticated device: %s", authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
        }

        return authenticatedDevices;
    }

    public static String findApplicationName(final Assertion assertion) {

        String applicationName = null;
        List<AudienceRestriction> audienceRestrictions = assertion.getConditions().getAudienceRestrictions();
        if (!audienceRestrictions.isEmpty()) {
            List<Audience> audiences = audienceRestrictions.get( 0 ).getAudiences();
            if (!audiences.isEmpty())
                applicationName = audiences.get( 0 ).getAudienceURI();
        }

        return applicationName;
    }
}
