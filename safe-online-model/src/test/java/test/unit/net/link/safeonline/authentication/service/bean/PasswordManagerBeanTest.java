package test.unit.net.link.safeonline.authentication.service.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.device.backend.bean.PasswordManagerBean;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class PasswordManagerBeanTest {

    private EntityTestManager entityTestManager;

    private PasswordManager   testedInstance;

    private AttributeTypeDAO  attributeTypeDAO;

    private AttributeDAO      attributeDAO;

    private SubjectDAO        subjectDAO;


    @Before
    public void setUp() throws Exception {

        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

        this.testedInstance = new PasswordManagerBean();
        this.attributeDAO = new AttributeDAOBean();
        this.attributeTypeDAO = new AttributeTypeDAOBean();
        this.subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(this.attributeDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.attributeTypeDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.subjectDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.testedInstance, this.attributeDAO);
        EJBTestUtils.inject(this.testedInstance, this.attributeTypeDAO);

        EJBTestUtils.init(this.attributeDAO);
        EJBTestUtils.init(this.attributeTypeDAO);
        EJBTestUtils.init(this.testedInstance);

        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE, DatatypeType.STRING,
                false, false));
        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE, DatatypeType.STRING,
                false, false));
        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.PASSWORD_DEVICE_DISABLE_ATTRIBUTE,
                DatatypeType.BOOLEAN, false, false));
        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE,
                DatatypeType.STRING, false, false));
        this.attributeTypeDAO.addAttributeType(new AttributeTypeEntity(SafeOnlineConstants.PASSWORD_DEVICE_ATTRIBUTE,
                DatatypeType.COMPOUNDED, false, false));

    }

    @After
    public void tearDown() throws Exception {

        this.entityTestManager.tearDown();
    }

    @Test
    public void testSetPassword() throws Exception {

        // prepare
        UUID subjectUUID = UUID.randomUUID();
        SubjectEntity subject = this.subjectDAO.addSubject(subjectUUID.toString());
        String password = "password";

        // operate
        this.testedInstance.setPassword(subject, password);

        // validate
        boolean validationResult = this.testedInstance.validatePassword(subject, password);
        assertTrue(validationResult);

        try {
            this.testedInstance.setPassword(subject, password);
            fail();
        } catch (PermissionDeniedException e) {
            // empty
        }

    }

    @Test
    public void testChangePassword() throws Exception {

        // prepare
        UUID subjectUUID = UUID.randomUUID();
        SubjectEntity subject = this.subjectDAO.addSubject(subjectUUID.toString());
        String password = "password";
        String newPassword = "newpassword";

        // operate
        this.testedInstance.setPassword(subject, password);
        this.testedInstance.changePassword(subject, password, newPassword);

        // validate
        boolean validationResult = this.testedInstance.validatePassword(subject, password);
        assertFalse(validationResult);

        validationResult = this.testedInstance.validatePassword(subject, newPassword);
        assertTrue(validationResult);
    }

    @Test
    public void validatePassword() throws Exception {

        // prepare
        UUID subjectUUID = UUID.randomUUID();
        SubjectEntity subject = this.subjectDAO.addSubject(subjectUUID.toString());
        String password = "password";

        // operate
        this.testedInstance.setPassword(subject, password);

        // validate
        boolean validationResult = this.testedInstance.validatePassword(subject, password);
        assertTrue(validationResult);
    }

    @Test
    public void testIsPasswordConfigured() throws Exception {

        // prepare
        UUID subjectUUID = UUID.randomUUID();
        SubjectEntity subject = this.subjectDAO.addSubject(subjectUUID.toString());
        String password = "password";

        // operate
        this.testedInstance.setPassword(subject, password);

        // validate
        boolean validationResult = this.testedInstance.isPasswordConfigured(subject);
        assertTrue(validationResult);
    }

}
