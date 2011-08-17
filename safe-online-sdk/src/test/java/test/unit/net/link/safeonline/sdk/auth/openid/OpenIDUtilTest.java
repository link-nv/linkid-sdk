/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.openid;

import static org.junit.Assert.*;

import java.util.*;
import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIdUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openid4java.message.ax.FetchResponse;


/**
 * Unit test for the OpenID utilitu class {@link OpenIdUtil}
 *
 * @author wvdhaute
 */
public class OpenIDUtilTest {

    private static final Log LOG = LogFactory.getLog( OpenIDUtilTest.class );

    @Test
    public void testFetchResponse()
            throws Exception {

        // Setup
        AttributeType singleAttributeType = new AttributeType( "test-single", DataType.STRING, null, true, true, false, false, false );
        AttributeType multiAttributeType = new AttributeType( "test-multi", DataType.STRING, null, true, true, true, false, false );

        AttributeType memberAttributeTypeA = new AttributeType( "test-member-1", DataType.STRING, null, true, true, true, false, false );
        AttributeType memberAttributeTypeB = new AttributeType( "test-member-2", DataType.DATE, null, true, true, true, false, false );

        AttributeType compoundAttributeType = new AttributeType( "test-compound", DataType.COMPOUNDED, null, true, true, true, false,
                false );
        compoundAttributeType.getMembers().add( memberAttributeTypeA );
        compoundAttributeType.getMembers().add( memberAttributeTypeB );

        AttributeCore singleAttribute = new AttributeCore( UUID.randomUUID().toString(), singleAttributeType,
                UUID.randomUUID().toString() );
        AttributeCore multiAttribute1 = new AttributeCore( UUID.randomUUID().toString(), multiAttributeType, UUID.randomUUID().toString() );
        AttributeCore multiAttribute2 = new AttributeCore( UUID.randomUUID().toString(), multiAttributeType, UUID.randomUUID().toString() );

        String compoundId1 = UUID.randomUUID().toString();
        String compoundId2 = UUID.randomUUID().toString();

        AttributeCore memberAttributeA1 = new AttributeCore( compoundId1, memberAttributeTypeA, UUID.randomUUID().toString() );
        AttributeCore memberAttributeB1 = new AttributeCore( compoundId1, memberAttributeTypeB, new Date() );
        AttributeCore memberAttributeA2 = new AttributeCore( compoundId2, memberAttributeTypeA, UUID.randomUUID().toString() );
        AttributeCore memberAttributeB2 = new AttributeCore( compoundId2, memberAttributeTypeB, new Date() );

        AttributeCore compoundAttribute1 = new AttributeCore( compoundId1, compoundAttributeType,
                new Compound( Arrays.asList( memberAttributeA1, memberAttributeB1 ) ) );
        AttributeCore compoundAttribute2 = new AttributeCore( compoundId2, compoundAttributeType,
                new Compound( Arrays.asList( memberAttributeA2, memberAttributeB2 ) ) );

        Map<String, List<AttributeCore>> attributeMap = new HashMap<String, List<AttributeCore>>();
        attributeMap.put( singleAttributeType.getName(), Arrays.asList( singleAttribute ) );
        attributeMap.put( multiAttributeType.getName(), Arrays.asList( multiAttribute1, multiAttribute2 ) );
        attributeMap.put( compoundAttributeType.getName(), Arrays.asList( compoundAttribute1, compoundAttribute2 ) );

        // Operate
        FetchResponse fetchResponse = OpenIdUtil.getFetchResponse( attributeMap, null, null );

        // Verify
        assertNotNull( fetchResponse );

        assertNotNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 0 ) );
        assertNotNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 1 ) );
        assertNotNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 2 ) );
        assertNotNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 3 ) );
        assertNotNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 4 ) );
        assertNull( fetchResponse.getAttributeAlias( OpenIdUtil.TYPE_URI_ATTRIBUTE_ID + 5 ) );

        assertEquals( 26, fetchResponse.getAttributeTypes().size() );

        // Operate
        Map<String, List<AttributeSDK<?>>> resultAttributeMap = OpenIdUtil.getAttributeMap( fetchResponse );

        // Verify
        assertNotNull( resultAttributeMap );
        assertEquals( 3, resultAttributeMap.size() );

        List<AttributeSDK<?>> resultSingleAttributes = resultAttributeMap.get( singleAttributeType.getName() );
        assertEquals( 1, resultSingleAttributes.size() );
        assertEquals( singleAttribute.getId(), resultSingleAttributes.get( 0 ).getId() );
        assertEquals( singleAttribute.getValue(), resultSingleAttributes.get( 0 ).getValue() );

        List<AttributeSDK<?>> resultMultiAttributes = resultAttributeMap.get( multiAttributeType.getName() );
        assertEquals( 2, resultMultiAttributes.size() );

        List<AttributeSDK<?>> resultCompoundAttributes = resultAttributeMap.get( compoundAttributeType.getName() );
        assertEquals( 2, resultCompoundAttributes.size() );
        assertEquals( 2, ((Compound) resultCompoundAttributes.get( 0 ).getValue()).getMembers().size() );
        assertEquals( 2, ((Compound) resultCompoundAttributes.get( 1 ).getValue()).getMembers().size() );
    }
}
