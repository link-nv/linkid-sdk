package test.unit.net.link.safeonline.digipass.webapp;

import java.util.UUID;

import javax.servlet.http.HttpServlet;

import net.link.safeonline.audit.bean.SecurityAuditLoggerBean;
import net.link.safeonline.audit.dao.bean.AccessAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditContextDAOBean;
import net.link.safeonline.audit.dao.bean.ResourceAuditDAOBean;
import net.link.safeonline.audit.dao.bean.SecurityAuditDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubjectIdentifierDAOBean;
import net.link.safeonline.demo.wicket.test.AbstractWicketTests;
import net.link.safeonline.digipass.webapp.DigipassApplication;
import net.link.safeonline.digipass.webapp.MainPage;
import net.link.safeonline.digipass.webapp.RegisterPage;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.digipass.bean.DigipassDeviceServiceBean;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;
import net.link.safeonline.service.bean.SubjectServiceBean;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


public class DigipassWebTest extends AbstractWicketTests {

    /**
     * Registers a digipass device.
     */
    @Test
    public void testRegisterDigipass() {

        // MainPage: Verify.
        this.wicket.assertRenderedPage(MainPage.class);
        this.wicket.assertPageLink("register", RegisterPage.class);
        this.wicket.assertPageLink("remove", RegisterPage.class);

        // MainPage: Click to register digipass
        this.wicket.clickLink("register");

        // RegisterPage: Verify.
        this.wicket.assertRenderedPage(RegisterPage.class);
        this.wicket.assertComponent("register_form", Form.class);

        // RegisterPage: Register digipass for user
        FormTester registerForm = this.wicket.newFormTester("register_form");
        registerForm.setValue("login", UUID.randomUUID().toString());
        registerForm.setValue("serialNumber", "12345678");
        registerForm.submit();

        // MainPage: Verify.
        this.wicket.assertRenderedPage(MainPage.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends HttpServlet> getLogoutServlet() {

        return LogoutServlet.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebApplication getApplication() {

        return new DigipassApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServiceBeans() {

        return new Class[] { DigipassDeviceServiceBean.class, SubjectServiceBean.class, SubjectDAOBean.class, AttributeDAOBean.class,
                AttributeTypeDAOBean.class, SubjectIdentifierDAOBean.class, IdGeneratorBean.class, DeviceDAOBean.class,
                SecurityAuditLoggerBean.class, AuditAuditDAOBean.class, AuditContextDAOBean.class, AccessAuditDAOBean.class,
                SecurityAuditDAOBean.class, ResourceAuditDAOBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class[] { SubjectEntity.class };
    }
}
