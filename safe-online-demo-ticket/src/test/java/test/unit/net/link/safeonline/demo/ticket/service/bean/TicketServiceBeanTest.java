/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.demo.ticket.service.bean;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.demo.ticket.entity.Ticket;
import net.link.safeonline.demo.ticket.entity.User;
import net.link.safeonline.demo.ticket.entity.Ticket.Site;
import net.link.safeonline.demo.ticket.service.TicketService;
import net.link.safeonline.demo.ticket.service.bean.TicketServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.joda.time.DateTime;

public class TicketServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(Ticket.class, User.class);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testValidPass() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = Site.GENT.name();
		String testTo = Site.BRUSSEL.name();
		String testUser = "test-user";
		Date beginDate = new Date();
		Date endDate = new DateTime(beginDate).plusMonths(1).toDate();

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		TicketService ticketService = EJBTestUtils.newInstance(
				TicketServiceBean.class, null, entityManager);

		User user = new User(testUser, testNrn);
		entityManager.persist(user);
		Ticket ticket = new Ticket(user, Site.GENT, Site.BRUSSEL, beginDate,
				endDate, false);
		entityManager.persist(ticket);

		// operate
		boolean result = ticketService.hasValidPass(testNrn, testFrom, testTo);

		// verify
		assertTrue(result);
	}

	public void testExpiredPass() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = Site.GENT.name();
		String testTo = Site.BRUSSEL.name();
		String testUser = "test-user-" + getName();
		DateTime now = new DateTime();
		Date beginDate = now.minusMonths(2).toDate();
		Date endDate = new DateTime(beginDate).plusMonths(1).toDate();

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		TicketService ticketService = EJBTestUtils.newInstance(
				TicketServiceBean.class, null, entityManager);

		User user = new User(testUser, testNrn);
		entityManager.persist(user);
		Ticket ticket = new Ticket(user, Site.GENT, Site.BRUSSEL, beginDate,
				endDate, false);
		entityManager.persist(ticket);

		// operate
		boolean result = ticketService.hasValidPass(testNrn, testFrom, testTo);

		// verify
		assertFalse(result);
	}

	public void testBidirectionalPass() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = Site.GENT.name();
		String testTo = Site.BRUSSEL.name();
		String testUser = "test-user";
		Date beginDate = new Date();
		Date endDate = new DateTime(beginDate).plusMonths(1).toDate();

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		TicketService ticketService = EJBTestUtils.newInstance(
				TicketServiceBean.class, null, entityManager);

		User user = new User(testUser, testNrn);
		entityManager.persist(user);
		Ticket ticket = new Ticket(user, Site.GENT, Site.BRUSSEL, beginDate,
				endDate, true);
		entityManager.persist(ticket);

		// operate
		boolean result = ticketService.hasValidPass(testNrn, testTo, testFrom);

		// verify
		assertTrue(result);
	}

	public void testInvalidFrom() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = "foobar-from";
		String testTo = Site.BRUSSEL.name();
		String testUser = "test-user";

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		TicketService ticketService = EJBTestUtils.newInstance(
				TicketServiceBean.class, null, entityManager);

		User user = new User(testUser, testNrn);
		entityManager.persist(user);

		// operate
		boolean result = ticketService.hasValidPass(testNrn, testTo, testFrom);

		// verify
		assertFalse(result);
	}
}
