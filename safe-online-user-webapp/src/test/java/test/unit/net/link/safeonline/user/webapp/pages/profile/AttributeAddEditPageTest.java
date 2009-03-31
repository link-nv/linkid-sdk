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
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.profile.AttributeAddEditPage;
import net.link.safeonline.user.webapp.pages.profile.ProfilePage;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.components.attribute.AttributeInputPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class AttributeAddEditPageTest {

    private WicketTester    wicket;

    private SubjectService  mockSubjectService;
    private IdentityService mockIdentityService;

    private JndiTestUtils   jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

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
    public void testPageEdit()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String username = "test-user-name";
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        AttributeDO attribute = new AttributeDO("test-attribute", DatatypeType.STRING);
        attribute.setStringValue("test-attribute-1-value");
        attribute.setEditable(true);
        List<AttributeDO> attributeContext = new LinkedList<AttributeDO>();
        attributeContext.add(attribute);

        // stubs
        expect(mockIdentityService.getAttributeEditContext(attribute)).andReturn(attributeContext);

        // prepare
        replay(mockIdentityService);

        // operate
        AttributeAddEditPage attributeAddEditPage = new AttributeAddEditPage(attribute, false);
        wicket.startPage(attributeAddEditPage);

        // verify
        verify(mockIdentityService);

        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ATTRIBUTE_CONTEXT_ID, attributeContext);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ATTRIBUTE_CONTEXT_ID + ":0:" + AttributeAddEditPage.ATTRIBUTE_ID, AttributeInputPanel.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ADD_BUTTON_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.SAVE_BUTTON_ID, Button.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.CANCEL_BUTTON_ID, Button.class);

        // reset stubs
        reset(mockIdentityService);

        // stubs
        mockIdentityService.saveAttribute(attribute);
        expect(mockSubjectService.getSubjectLogin(userId)).andReturn(username);
        expect(mockIdentityService.listAttributes(wicket.getWicketSession().getLocale())).andReturn(new LinkedList<AttributeDO>());

        // prepare
        replay(mockIdentityService, mockSubjectService);

        // operate
        FormTester formTester = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM);
        formTester.submit(AttributeAddEditPage.SAVE_BUTTON_ID);

        // verify
        verify(mockIdentityService, mockSubjectService);

        wicket.assertRenderedPage(ProfilePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ProfilePage.PROFILE_EMPTY_LABEL_ID, "profileEmpty");
    }

    @Test
    public void testPageAdd()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String username = "test-user-name";
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        AttributeDO attribute = new AttributeDO("test-attribute", DatatypeType.STRING);
        attribute.setEditable(true);
        attribute.setMultivalued(true);
        List<AttributeDO> attributeContext = new LinkedList<AttributeDO>();
        attributeContext.add(attribute);

        // stubs
        expect(mockIdentityService.getAttributeTemplate(attribute)).andReturn(attributeContext);

        // prepare
        replay(mockIdentityService);

        // operate
        AttributeAddEditPage attributeAddEditPage = new AttributeAddEditPage(attribute, true);
        wicket.startPage(attributeAddEditPage);

        // verify
        verify(mockIdentityService);

        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ATTRIBUTE_CONTEXT_ID, attributeContext);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ATTRIBUTE_CONTEXT_ID + ":0:" + AttributeAddEditPage.ATTRIBUTE_ID, AttributeInputPanel.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.ADD_BUTTON_ID, Button.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.SAVE_BUTTON_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM + ":"
                + AttributeAddEditPage.CANCEL_BUTTON_ID, Button.class);

        // reset stubs
        reset(mockIdentityService);

        // stubs
        mockIdentityService.addAttribute(attributeContext);
        expect(mockSubjectService.getSubjectLogin(userId)).andReturn(username);
        expect(mockIdentityService.listAttributes(wicket.getWicketSession().getLocale())).andReturn(new LinkedList<AttributeDO>());

        // prepare
        replay(mockIdentityService, mockSubjectService);

        // operate
        FormTester formTester = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AttributeAddEditPage.ATTRIBUTE_ADD_FORM);
        formTester.setValue(AttributeAddEditPage.ATTRIBUTE_CONTEXT_ID + ":0:" + AttributeAddEditPage.ATTRIBUTE_ID + ":"
                + AttributeInputPanel.STRING_ID, "new-value");
        formTester.submit(AttributeAddEditPage.ADD_BUTTON_ID);

        // verify
        verify(mockIdentityService, mockSubjectService);

        wicket.assertRenderedPage(ProfilePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ProfilePage.PROFILE_EMPTY_LABEL_ID, "profileEmpty");
    }

}
