package test.unit.net.link.safeonline.authentication.service.bean;

import junit.framework.TestCase;
import net.link.safeonline.authentication.dao.EntityDAO;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private EntityDAO mockEntityDAO;

	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new AuthenticationServiceBean();

		this.mockEntityDAO = EasyMock.createMock(EntityDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockEntityDAO);
	}

	public void testAuthenticate() throws Exception {
		// setup
		String username = "test-username";
		String password = "test-password";

		// stubs
		EasyMock.expect(this.mockEntityDAO.findEntity(username)).andStubReturn(
				new EntityEntity(username, password));

		// prepare
		EasyMock.replay(this.mockEntityDAO);

		// operate
		boolean result = this.testedInstance.authenticate(username, password);

		// verify
		EasyMock.verify(this.mockEntityDAO);
		assertTrue(result);
	}
}
