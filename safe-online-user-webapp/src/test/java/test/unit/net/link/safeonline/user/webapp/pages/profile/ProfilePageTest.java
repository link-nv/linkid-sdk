package test.unit.net.link.safeonline.user.webapp.pages.profile;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.profile.ProfilePage;
import net.link.safeonline.webapp.components.attribute.AttributeNameOutputPanel;
import net.link.safeonline.webapp.components.attribute.AttributeOutputPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class ProfilePageTest {

    private WicketTester    wicket;

    private SubjectService  mockSubjectService;
    private IdentityService mockIdentityService;

    private JndiTestUtils   jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new OlasNamingStrategy());

        mockSubjectService = createMock(SubjectService.class);
        mockIdentityService = createMock(IdentityService.class);
        jndiTestUtils.bindComponent(SubjectService.class, mockSubjectService);
        jndiTestUtils.bindComponent(IdentityService.class, mockIdentityService);

        wicket = new WicketTester(new UserTestApplication());
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void testEmptyProfilePage()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String username = "test-user-name";
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        // stubs
        expect(mockSubjectService.getSubjectLogin(userId)).andReturn(username);
        expect(mockIdentityService.listAttributes(wicket.getWicketSession().getLocale())).andReturn(new LinkedList<AttributeDO>());

        // prepare
        replay(mockSubjectService, mockIdentityService);

        // operate
        wicket.startPage(ProfilePage.class);

        // verify
        verify(mockSubjectService, mockIdentityService);

        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ProfilePage.PROFILE_EMPTY_LABEL_ID, "profileEmpty");

    }

    @Test
    public void testPage()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String username = "test-user-name";
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        AttributeDO attribute1 = new AttributeDO("test-attribute-1", DatatypeType.STRING);
        attribute1.setStringValue("test-attribute-1-value");
        attribute1.setEditable(true);
        AttributeDO attribute2 = new AttributeDO("test-attribute-2", DatatypeType.STRING);
        attribute2.setStringValue("test-attribute-2-value");
        attribute2.setEditable(true);
        attribute2.setMultivalued(true);
        List<AttributeDO> attributes = new LinkedList<AttributeDO>();
        attributes.add(attribute1);
        attributes.add(attribute2);

        // stubs
        expect(mockSubjectService.getSubjectLogin(userId)).andReturn(username);
        expect(mockIdentityService.listAttributes(wicket.getWicketSession().getLocale())).andReturn(attributes);

        // prepare
        replay(mockSubjectService, mockIdentityService);

        // operate
        wicket.startPage(ProfilePage.class);

        // verify
        verify(mockSubjectService, mockIdentityService);

        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + ProfilePage.PROFILE_EMPTY_LABEL_ID);
        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID, attributes);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.ATTRIBUTE_NAME_ID,
                AttributeNameOutputPanel.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.ATTRIBUTE_VALUE_ID,
                AttributeOutputPanel.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.EDIT_LINK_ID, Link.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.ADD_LINK_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.REMOVE_LINK_ID, Link.class);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":1:" + ProfilePage.ATTRIBUTE_NAME_ID,
                AttributeNameOutputPanel.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":1:" + ProfilePage.ATTRIBUTE_VALUE_ID,
                AttributeOutputPanel.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":1:" + ProfilePage.EDIT_LINK_ID, Link.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":1:" + ProfilePage.ADD_LINK_ID, Link.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":1:" + ProfilePage.REMOVE_LINK_ID, Link.class);

        // reset stubs
        reset(mockSubjectService, mockIdentityService);

        // stubs
        mockIdentityService.removeAttribute(attribute1);
        expect(mockIdentityService.listAttributes(wicket.getWicketSession().getLocale())).andReturn(new LinkedList<AttributeDO>());

        // prepare
        replay(mockIdentityService);

        // operate
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + ProfilePage.ATTRIBUTES_ID + ":0:" + ProfilePage.REMOVE_LINK_ID);

        // verify
        verify(mockIdentityService);

    }
}
