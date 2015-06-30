/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.attribute.LinkIDCompound;
import net.link.safeonline.sdk.api.ws.LinkIDWebServiceConstants;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponse;
import net.link.util.InternalInconsistencyException;
import net.link.util.saml.Saml2Utils;
import net.link.util.saml.SamlUtils;
import net.link.util.util.ConversionUtils;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.NamespaceManager;
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

    public static <X extends XMLObject> X unmarshall(Element xmlElement) {

        X xmlObject = Saml2Utils.unmarshall( xmlElement );
        NamespaceManager xmlObjectNSM = new NamespaceManager( xmlObject );
        xmlObjectNSM.registerNamespace( new Namespace( LinkIDWebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE, LinkIDWebServiceConstants.SAFE_ONLINE_SAML_PREFIX ) );

        return xmlObject;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<LinkIDAttribute<Serializable>>> getAttributeValues(Assertion assertion) {

        Map<String, List<LinkIDAttribute<Serializable>>> attributeMap = Maps.newHashMap();
        List<AttributeStatement> attrStatements = assertion.getAttributeStatements();
        if (attrStatements == null || attrStatements.isEmpty())
            return ImmutableMap.of();

        AttributeStatement attributeStatement = attrStatements.get( 0 );

        for (Attribute attribute : attributeStatement.getAttributes()) {

            LinkIDAttribute<Serializable> linkIDAttribute = getAttributeSDK( attribute );

            List<LinkIDAttribute<Serializable>> attributes = attributeMap.get( linkIDAttribute.getName() );
            if (null == attributes) {
                attributes = new LinkedList<LinkIDAttribute<Serializable>>();
            }
            attributes.add( linkIDAttribute );
            attributeMap.put( linkIDAttribute.getName(), attributes );
        }

        return attributeMap;
    }

    private static LinkIDAttribute<Serializable> getAttributeSDK(Attribute attributeType) {

        String attributeId = attributeType.getUnknownAttributes().get( LinkIDWebServiceConstants.ATTRIBUTE_ID );
        LinkIDAttribute<Serializable> attribute = new LinkIDAttribute<Serializable>( attributeId, attributeType.getName(), null );

        List<XMLObject> attributeValues = attributeType.getAttributeValues();
        if (attributeValues.isEmpty())
            return attribute;

        XMLObject xmlValue = attributeValues.get( 0 );
        if (null != xmlValue.getOrderedChildren() && !xmlValue.getOrderedChildren().isEmpty()) {

            // compound
            List<LinkIDAttribute<?>> compoundMembers = new LinkedList<LinkIDAttribute<?>>();
            for (XMLObject memberAttributeObject : attributeValues.get( 0 ).getOrderedChildren()) {

                Attribute memberAttribute = (Attribute) memberAttributeObject;
                LinkIDAttribute<Serializable> member = new LinkIDAttribute<Serializable>( attributeId, memberAttribute.getName(), null );
                if (!memberAttribute.getAttributeValues().isEmpty()) {
                    member.setValue( toJavaObject( memberAttribute.getAttributeValues().get( 0 ) ) );
                }
                compoundMembers.add( member );
            }
            attribute.setValue( new LinkIDCompound( compoundMembers ) );
        } else {
            // single/multi valued
            attribute.setValue( toJavaObject( xmlValue ) );
        }

        return attribute;
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

    @Nullable
    public static net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse findPaymentResponse(final Response samlResponse) {

        if (null == samlResponse.getExtensions())
            return null;

        if (null == samlResponse.getExtensions().getUnknownXMLObjects( LinkIDPaymentResponse.DEFAULT_ELEMENT_NAME ))
            return null;

        List<XMLObject> paymentResponses = samlResponse.getExtensions().getUnknownXMLObjects( LinkIDPaymentResponse.DEFAULT_ELEMENT_NAME );
        if (paymentResponses.size() > 1) {
            logger.err( "Only 1 PaymentResponse in the Response extensions element is supported" );
            throw new InternalInconsistencyException( "Failed to parse SAML2 response: Only 1 PaymentResponse in the Response extensions element is supported",
                    null );
        }

        if (paymentResponses.isEmpty()) {
            return null;
        }

        LinkIDPaymentResponse paymentResponse = (LinkIDPaymentResponse) paymentResponses.get( 0 );
        Map<String, String> paymentResponseMap = Maps.newHashMap();
        for (Attribute attribute : paymentResponse.getAttributes()) {
            String name = attribute.getName();
            List<XMLObject> attributeValues = attribute.getAttributeValues();
            if (!attributeValues.isEmpty()) {
                paymentResponseMap.put( name, ConversionUtils.toString( SamlUtils.toJavaObject( attributeValues.get( 0 ) ) ) );
            }
        }
        return net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse.fromMap( paymentResponseMap );
    }

    @Nullable
    public static net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse findExternalCodeResponse(final Response samlResponse) {

        if (null == samlResponse.getExtensions())
            return null;

        if (null == samlResponse.getExtensions().getUnknownXMLObjects( LinkIDExternalCodeResponse.DEFAULT_ELEMENT_NAME ))
            return null;

        List<XMLObject> externalCodeResponses = samlResponse.getExtensions().getUnknownXMLObjects( LinkIDExternalCodeResponse.DEFAULT_ELEMENT_NAME );
        if (externalCodeResponses.size() > 1) {
            logger.err( "Only 1 ExternalCodeResponse in the Response extensions element is supported" );
            throw new InternalInconsistencyException(
                    "Failed to parse SAML2 response: Only 1 ExternalCodeResponse in the Response extensions element is supported", null );
        }

        if (externalCodeResponses.isEmpty()) {
            return null;
        }

        LinkIDExternalCodeResponse linkIDExternalCodeResponse = (LinkIDExternalCodeResponse) externalCodeResponses.get( 0 );
        Map<String, String> externalCodeResponseMap = Maps.newHashMap();
        for (Attribute attribute : linkIDExternalCodeResponse.getAttributes()) {
            String name = attribute.getName();
            List<XMLObject> attributeValues = attribute.getAttributeValues();
            if (!attributeValues.isEmpty()) {
                externalCodeResponseMap.put( name, ConversionUtils.toString( SamlUtils.toJavaObject( attributeValues.get( 0 ) ) ) );
            }
        }
        return net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse.fromMap( externalCodeResponseMap );
    }
}