package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.authentication.service.bean.PasswordManagerBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;

import org.junit.Before;
import org.junit.Test;

public class PasswordManagerBeanTest {

	private PasswordManager testedInstance;

	private AttributeTypeDAO mockAttributeTypeDAO;

	private AttributeDAO mockAttributeDAO;

	private Object[] mockObjects;

	@Before
	public void setUp() throws Exception {
		this.testedInstance = new PasswordManagerBean();

		this.mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockAttributeTypeDAO);

		this.mockAttributeDAO = createMock(AttributeDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockAttributeDAO);

		this.mockObjects = new Object[] { this.mockAttributeDAO,
				this.mockAttributeTypeDAO };

	}

	@Test
	public void testSetPassword() throws Exception {
		// prepare
		SubjectEntity subject = new SubjectEntity("test-subject");
		String password = "password";

		AttributeTypeEntity passwordType = new AttributeTypeEntity();

		// stubs
		expect(
				this.mockAttributeTypeDAO
						.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(
				this.mockAttributeTypeDAO
						.findAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(this.mockAttributeDAO.findAttribute(passwordType, subject))
				.andStubReturn(null);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, subject
								.getLogin())).andStubReturn(null);

		expect(
				this.mockAttributeDAO.addAttribute(passwordType, subject,
						password)).andReturn(null);

		// replay
		replay(this.mockObjects);

		// operate
		this.testedInstance.setPassword(subject, password, false);

		// validate
		verify(this.mockObjects);

	}

	@Test
	public void testSetPasswordWithPreviousPasswordSet() throws Exception {
		// prepare
		SubjectEntity subject = new SubjectEntity("test-subject");
		String password = "password";

		AttributeTypeEntity passwordType = new AttributeTypeEntity();
		AttributeEntity passwordEntity = new AttributeEntity(passwordType,
				subject, "something");

		// stubs
		expect(
				this.mockAttributeTypeDAO
						.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(
				this.mockAttributeTypeDAO
						.findAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(this.mockAttributeDAO.findAttribute(passwordType, subject))
				.andStubReturn(passwordEntity);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, subject
								.getLogin())).andStubReturn(passwordEntity);

		// replay
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.setPassword(subject, password, false);
			fail();
		} catch (PermissionDeniedException e) {
		}

		this.testedInstance.setPassword(subject, password, true);

		assertEquals(passwordEntity.getStringValue(), password);

		// validate
		verify(this.mockObjects);

	}

	@Test
	public void testChangePassword() throws Exception {
		// prepare
		SubjectEntity subject = new SubjectEntity("test-subject");
		String password = "password";
		String newPassword = "newpassword";

		AttributeTypeEntity passwordType = new AttributeTypeEntity();
		AttributeEntity passwordEntity = new AttributeEntity(passwordType,
				subject, password);

		// stubs
		expect(
				this.mockAttributeTypeDAO
						.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(
				this.mockAttributeTypeDAO
						.findAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(this.mockAttributeDAO.findAttribute(passwordType, subject))
				.andStubReturn(passwordEntity);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, subject
								.getLogin())).andStubReturn(passwordEntity);

		// replay
		replay(this.mockObjects);

		// operate
		try {
			this.testedInstance.changePassword(subject, newPassword,
					newPassword);
			fail();
		} catch (PermissionDeniedException e) {
		}

		this.testedInstance.changePassword(subject, password, newPassword);

		assertEquals(passwordEntity.getStringValue(), newPassword);

		// validate
		verify(this.mockObjects);
	}

	@Test
	public void testValidatePassword() throws Exception {
		// prepare
		SubjectEntity subject = new SubjectEntity("test-subject");
		String password = "password";
		String wrongPassword = "wrongpassword";

		AttributeTypeEntity passwordType = new AttributeTypeEntity();
		AttributeEntity passwordEntity = new AttributeEntity(passwordType,
				subject, password);

		// stubs
		expect(
				this.mockAttributeTypeDAO
						.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(
				this.mockAttributeTypeDAO
						.findAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(this.mockAttributeDAO.findAttribute(passwordType, subject))
				.andStubReturn(passwordEntity);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, subject
								.getLogin())).andStubReturn(passwordEntity);

		// replay
		replay(this.mockObjects);

		// operate

		assertFalse(this.testedInstance
				.validatePassword(subject, wrongPassword));

		assertTrue(this.testedInstance.validatePassword(subject, password));

		// validate
		verify(this.mockObjects);
	}

	@Test
	public void testIsPasswordConfigured() throws Exception {
		// prepare
		SubjectEntity subject = new SubjectEntity("test-subject");
		SubjectEntity wrongSubject = new SubjectEntity("wrong-subject");
		String password = "password";

		AttributeTypeEntity passwordType = new AttributeTypeEntity();
		AttributeEntity passwordEntity = new AttributeEntity(passwordType,
				subject, password);

		// stubs
		expect(
				this.mockAttributeTypeDAO
						.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(
				this.mockAttributeTypeDAO
						.findAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE))
				.andStubReturn(passwordType);

		expect(this.mockAttributeDAO.findAttribute(passwordType, subject))
				.andStubReturn(passwordEntity);
		expect(this.mockAttributeDAO.findAttribute(passwordType, wrongSubject))
				.andStubReturn(null);

		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, subject
								.getLogin())).andStubReturn(passwordEntity);
		expect(
				this.mockAttributeDAO.findAttribute(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, wrongSubject
								.getLogin())).andStubReturn(null);

		// replay
		replay(this.mockObjects);

		// operate

		assertFalse(this.testedInstance.isPasswordConfigured(wrongSubject));

		assertTrue(this.testedInstance.isPasswordConfigured(subject));

		// validate
		verify(this.mockObjects);
	}

}
