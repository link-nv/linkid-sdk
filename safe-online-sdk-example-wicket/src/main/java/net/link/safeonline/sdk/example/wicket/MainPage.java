package net.link.safeonline.sdk.example.wicket;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.xkms2.Xkms2Client;
import net.link.safeonline.wicket.component.linkid.LinkIDApplicationPage;
import net.link.safeonline.wicket.component.linkid.LinkIDLoginLink;
import net.link.safeonline.wicket.component.linkid.LinkIDLogoutLink;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;


public class MainPage extends LinkIDApplicationPage {

    private String attributes   = "";
    private String wsAttributes = "";

    /**
     * Add components to the layout that are present on every page.
     *
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
                return attributes;
            }
        } ) );

        add( new Label( "ws_attributes", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return wsAttributes;
            }
        } ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        if (ExampleSession.get().isUserSet()) {

            // Fetch application's identity through LinkID's Attribute WS.
            AttributeClient attributeClient = LinkIDServiceFactory.getAttributeService();
            try {
                Map<String, List<AttributeSDK<?>>> attributeMap = attributeClient.getAttributes( ExampleSession.get().findUserLinkID() );
                for (Map.Entry<String, List<AttributeSDK<?>>> attributeEntry : attributeMap.entrySet()) {
                    for (AttributeSDK<?> attribute : attributeEntry.getValue()) {
                        wsAttributes += " " + attribute.getAttributeName() + "=" + attribute.getValue();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException( e );
            }

            // attribute in SAML response
            for (Map.Entry<String, List<AttributeSDK<?>>> attributeEntry : LinkIDWicketUtils.findAttributes().entrySet()) {

                for (AttributeSDK<?> attribute : attributeEntry.getValue()) {
                    attributes += " " + attribute.getAttributeName() + "=" + attribute.getValue();
                }
            }
        }

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLinkIDAuthenticated() {

        ExampleSession.get().setUserId( LinkIDWicketUtils.findLinkID() );

        // validate PKI via XKMS
        List<X509Certificate> linkIDCertificateChain = LinkIDWicketUtils.findCertificateChain();
        if (null != linkIDCertificateChain) {
            Xkms2Client xkms2Client = LinkIDServiceFactory.getXkms2Client();
            try {
                xkms2Client.validate( linkIDCertificateChain );
            } catch (WSClientTransportException e) {
                throw new RuntimeException( e );
            } catch (ValidationFailedException e) {
                throw new RuntimeException( e );
            } catch (CertificateEncodingException e) {
                throw new RuntimeException( e );
            }
        }
    }
}
