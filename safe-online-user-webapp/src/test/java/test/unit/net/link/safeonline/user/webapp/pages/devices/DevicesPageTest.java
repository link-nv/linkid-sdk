package test.unit.net.link.safeonline.user.webapp.pages.devices;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.devices.DevicesPage;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.components.attribute.AttributeOutputPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class DevicesPageTest {

    private WicketTester        wicket;

    private DevicePolicyService mockDevicePolicyService;
    private DeviceService       mockDeviceService;
    private SubjectManager      mockSubjectManager;

    private JndiTestUtils       jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

        mockDevicePolicyService = createMock(DevicePolicyService.class);
        mockDeviceService = createMock(DeviceService.class);
        mockSubjectManager = createMock(SubjectManager.class);
        jndiTestUtils.bindComponent(DevicePolicyService.class, mockDevicePolicyService);
        jndiTestUtils.bindComponent(DeviceService.class, mockDeviceService);
        jndiTestUtils.bindComponent(SubjectManager.class, mockSubjectManager);

        wicket = new WicketTester(new UserTestApplication());
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void testPage()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        SubjectEntity subject = new SubjectEntity(userId);

        String device1Name = "device-1-name";
        String device1FriendlyName = "device-1-friendly-name";
        DeviceEntity device1 = new DeviceEntity();
        device1.setName(device1Name);
        device1.setRemovalPath("device-1-remove-path");
        device1.setUpdatePath("device-1-update-path");
        device1.setDisablePath("device-1-disable-path");
        device1.setEnablePath("device-1-enable-path");

        String device2Name = "device-2-name";
        DeviceEntity device2 = new DeviceEntity();
        device2.setName(device2Name);
        device2.setRegistrationPath("device-2-registration-path");
        List<DeviceEntity> devices = new LinkedList<DeviceEntity>();
        devices.add(device1);
        devices.add(device2);

        String deviceRegistration1Id = UUID.randomUUID().toString();
        AttributeDO deviceRegistration1Attribute = new AttributeDO("device-registration-1-attribute", DatatypeType.STRING);
        deviceRegistration1Attribute.setStringValue("device-registration-1-attribute-value");
        DeviceRegistrationDO deviceRegistration1 = new DeviceRegistrationDO(device1, device1FriendlyName, deviceRegistration1Id,
                deviceRegistration1Attribute, false);
        List<DeviceRegistrationDO> deviceRegistrations = new LinkedList<DeviceRegistrationDO>();
        deviceRegistrations.add(deviceRegistration1);

        // stubs
        expect(mockSubjectManager.getCallerSubject()).andReturn(subject);
        expect(mockDevicePolicyService.getDevices()).andReturn(devices);
        expect(mockDevicePolicyService.getDeviceDescription(device1Name, wicket.getWicketSession().getLocale())).andReturn(device1Name);
        expect(mockDevicePolicyService.getDeviceDescription(device2Name, wicket.getWicketSession().getLocale())).andReturn(device2Name);
        expect(mockDeviceService.getDeviceRegistrations(subject, wicket.getWicketSession().getLocale())).andReturn(deviceRegistrations);

        // prepare
        replay(mockSubjectManager, mockDevicePolicyService, mockDeviceService);

        // operate
        wicket.startPage(DevicesPage.class);

        // verify
        verify(mockSubjectManager, mockDevicePolicyService, mockDeviceService);

        /*
         * Devices
         */
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID, ListView.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID + ":0:" + DevicesPage.NAME_ID, device1Name);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID + ":0:" + DevicesPage.REGISTER_LINK_ID);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID + ":1:" + DevicesPage.NAME_ID, device2Name);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID + ":1:" + DevicesPage.REGISTER_LINK_ID, Link.class);

        /*
         * Device registrations
         */
        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID, deviceRegistrations);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.REGISTERED_NAME_ID,
                deviceRegistrations.get(0).getFriendlyName());
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.INFORMATION_ID,
                AttributeOutputPanel.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.REMOVE_LINK_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.UPDATE_LINK_ID,
                Link.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.DISABLE_LINK_ID);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + DevicesPage.REGISTERED_DEVICES_ID + ":0:" + DevicesPage.ENABLE_LINK_ID);
    }

    @Test
    public void testRegisterDevice()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        SubjectEntity subject = new SubjectEntity(userId);

        String deviceName = "device-name";
        DeviceEntity device = new DeviceEntity();
        device.setName(deviceName);
        device.setRegistrationPath("device-registration-path");
        List<DeviceEntity> devices = new LinkedList<DeviceEntity>();
        devices.add(device);

        // stubs
        expect(mockSubjectManager.getCallerSubject()).andReturn(subject);
        expect(mockDevicePolicyService.getDevices()).andReturn(devices);
        expect(mockDevicePolicyService.getDeviceDescription(deviceName, wicket.getWicketSession().getLocale())).andReturn(deviceName);
        expect(mockDeviceService.getDeviceRegistrations(subject, wicket.getWicketSession().getLocale())).andReturn(
                new LinkedList<DeviceRegistrationDO>());

        // prepare
        replay(mockSubjectManager, mockDevicePolicyService, mockDeviceService);

        // operate
        wicket.startPage(DevicesPage.class);

        // verify
        verify(mockSubjectManager, mockDevicePolicyService, mockDeviceService);

        // reset stubs
        reset(mockSubjectManager, mockDevicePolicyService, mockDeviceService);

        // stubs
        expect(mockDevicePolicyService.getRegistrationURL(deviceName)).andReturn("device-registration-path");

        // prepare
        replay(mockDevicePolicyService);

        // operate
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + DevicesPage.DEVICES_ID + ":0:" + DevicesPage.REGISTER_LINK_ID);

        // verify
        verify(mockDevicePolicyService);
    }
}
