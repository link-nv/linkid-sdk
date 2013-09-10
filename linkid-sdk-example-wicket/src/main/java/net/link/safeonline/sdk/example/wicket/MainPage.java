package net.link.safeonline.sdk.example.wicket;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.attrib.client.AttributeClient;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.xkms2.Xkms2ClientImpl;
import net.link.safeonline.wicket.component.linkid.*;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import net.link.util.common.CertificateChain;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;


public class MainPage extends LinkIDApplicationPage {

    private final StringBuilder attributes   = new StringBuilder();
    private final StringBuilder wsAttributes = new StringBuilder();

    /**
     * Add components to the layout that are present on every page.
     * <p/>
     * This includes the title and the global ticket.
     */
    public MainPage() {

        add( new LinkIDLoginLink( "login" ).setVisible( !ExampleSession.get().isUserSet() ) );
        add( new LinkIDLogoutLink( "logout" ).setVisible( ExampleSession.get().isUserSet() ) );

        add( new Label( "userId", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return ExampleSession.get().isUserSet()? ExampleSession.get().findUserLinkID(): "";
            }
        } ) );

        add( new Label( "attributes", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return attributes.toString();
            }
        } ) );

        add( new Label( "ws_attributes", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return wsAttributes.toString();
            }
        } ) );
    }

    @Override
    protected void onBeforeRender() {

        if (ExampleSession.get().isUserSet()) {
            attributes.setLength( 0 );
            wsAttributes.setLength( 0 );

            // Fetch application's identity through LinkID's Attribute WS.
            AttributeClient attributeClient = LinkIDServiceFactory.getAttributeService();
            try {
                Map<String, List<AttributeSDK<Serializable>>> attributeMap = attributeClient.getAttributes(
                        ExampleSession.get().findUserLinkID() );
                for (Entry<String, List<AttributeSDK<Serializable>>> attributeEntry : attributeMap.entrySet())
                    for (AttributeSDK<?> attribute : attributeEntry.getValue())
                        wsAttributes.append( ' ' ).append( attribute.getName() ).append( '=' ).append( attribute.getValue() );
            }
            catch (AttributeUnavailableException e) {
                error( e );
            }
            catch (RequestDeniedException e) {
                error( e );
            }
            catch (WSClientTransportException e) {
                error( e );
            }
            catch (SubjectNotFoundException e) {
                error( e );
            }
            catch (AttributeNotFoundException e) {
                error( e );
            }

            // attribute in SAML response
            for (Entry<String, List<AttributeSDK<Serializable>>> attributeEntry : LinkIDWicketUtils.findAttributes().entrySet())
                for (AttributeSDK<?> attribute : attributeEntry.getValue())
                    attributes.append( ' ' ).append( attribute.getName() ).append( '=' ).append( attribute.getValue() );
        }

        super.onBeforeRender();
    }

    @Override
    protected void onLinkIDAuthenticated() {

        ExampleSession.get().setUserId( LinkIDWicketUtils.findLinkID() );

        // validate PKI via XKMS
        CertificateChain linkIDCertificateChain = LinkIDWicketUtils.findCertificateChain();
        if (null != linkIDCertificateChain) {
            Xkms2ClientImpl xkms2Client = (Xkms2ClientImpl) LinkIDServiceFactory.getXkms2Client();
            try {
                xkms2Client.validate( linkIDCertificateChain );
            }
            catch (WSClientTransportException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (CertificateEncodingException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (ValidationFailedException e) {
                throw new InternalInconsistencyException( e );
            }
        }
    }
}
