/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.docdata;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.attribute.Compound;


public class DocDataToken implements Serializable {

    private final String           attributeId;
    private final String           tokenId;
    private final DocDataTokenType tokenType;
    private final String           prettyPrint;
    private final int              priority;

    public DocDataToken(final String attributeId, final String tokenId, final DocDataTokenType tokenType, final String prettyPrint, final int priority) {

        this.attributeId = attributeId;
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.prettyPrint = prettyPrint;
        this.priority = priority;
    }

    public DocDataToken(final AttributeSDK<Serializable> docDataAttribute) {

        if (!docDataAttribute.getName().equals( DocDataConstants.TOKEN )) {
            throw new RuntimeException(
                    String.format( "Trying to initialize a DocData token with the wrong linkID attribute: %s", docDataAttribute.getName() ) );
        }

        Compound compound = (Compound) docDataAttribute.getValue();
        AttributeSDK<String> idAttribute = compound.findMember( DocDataConstants.TOKEN_ID );
        AttributeSDK<String> typeAttribute = compound.findMember( DocDataConstants.TOKEN_TYPE );
        AttributeSDK<String> prettyPintAttribute = compound.findMember( DocDataConstants.TOKEN_PRETTY_PRINT );
        AttributeSDK<Integer> priorityAttribute = compound.findMember( DocDataConstants.TOKEN_PRIORITY );

        if (null == idAttribute || null == typeAttribute || null == prettyPintAttribute || null == priorityAttribute) {
            throw new RuntimeException( "Incomplete DocData Token attribute" );
        }

        attributeId = docDataAttribute.getId();
        tokenId = idAttribute.getValue();
        tokenType = DocDataTokenType.valueOf( typeAttribute.getValue() );
        prettyPrint = prettyPintAttribute.getValue();
        priority = priorityAttribute.getValue();
    }

    public String getAttributeId() {

        return attributeId;
    }

    public String getTokenId() {

        return tokenId;
    }

    public DocDataTokenType getTokenType() {

        return tokenType;
    }

    public String getPrettyPrint() {

        return prettyPrint;
    }

    public int getPriority() {

        return priority;
    }

    public static List<DocDataToken> getTokens(final List<AttributeSDK<Serializable>> attributes) {

        List<DocDataToken> tokens = new LinkedList<DocDataToken>();
        for (AttributeSDK<Serializable> attribute : attributes) {
            tokens.add( new DocDataToken( attribute ) );
        }
        return tokens;
    }
}
