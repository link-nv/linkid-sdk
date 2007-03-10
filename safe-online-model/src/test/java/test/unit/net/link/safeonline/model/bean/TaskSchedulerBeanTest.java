/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.Startable;
import net.link.safeonline.Task;
import net.link.safeonline.dao.SchedulingDAO;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.bean.SchedulingDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.TaskDAOBean;
import net.link.safeonline.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.model.bean.TaskSchedulerBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;

public class TaskSchedulerBeanTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(TaskSchedulerBeanTest.class);

	private static Class[] container = new Class[] { SubjectDAOBean.class,
			ApplicationDAOBean.class, SubscriptionDAOBean.class,
			AttributeDAOBean.class, TrustDomainDAOBean.class,
			ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
			ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class,
			ConfigItemDAOBean.class, TaskDAOBean.class,
			SchedulingDAOBean.class, TaskHistoryDAOBean.class };

	private EntityTestManager entityTestManager;

	private TaskSchedulerBean testedInstance;

	private TimerService mockTimerService;

	private Timer mockTimer;

	private JndiTestUtils jndiTestUtils;

	@Override
	public void setUp() throws Exception {
		this.mockTimerService = createMock(TimerService.class);
		this.mockTimer = createMock(Timer.class);
		this.testedInstance = new TaskSchedulerBean();
		EJBTestUtils.inject(this.testedInstance, this.mockTimerService);

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, ApplicationOwnerEntity.class,
				AttributeEntity.class, AttributeTypeEntity.class,
				SubscriptionEntity.class, TrustDomainEntity.class,
				ApplicationIdentityEntity.class, ConfigGroupEntity.class,
				ConfigItemEntity.class, SchedulingEntity.class,
				TaskEntity.class, TaskHistoryEntity.class);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);

		systemStartable.postStart();

		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		this.jndiTestUtils.tearDown();
		super.tearDown();
	}

	public void testSetTimer() {
		// setup
		SchedulingEntity scheduling = new SchedulingEntity("test",
				"0 0/5 * * * ?", null);

		expect(
				this.mockTimerService.createTimer((Date) anyObject(),
						(String) anyObject())).andReturn(this.mockTimer);
		expect(this.mockTimer.getHandle()).andReturn(null);
		expect(
				this.mockTimerService.createTimer((Date) anyObject(),
						(String) anyObject())).andReturn(this.mockTimer);
		expect(this.mockTimer.getHandle()).andReturn(null);
		replay(this.mockTimerService);
		replay(this.mockTimer);

		// operate
		this.testedInstance.setTimer(scheduling);
		Date firstDate = scheduling.getFireDate();
		this.testedInstance.setTimer(scheduling);
		Date nextDate = scheduling.getFireDate();
		assertFalse(firstDate.equals(nextDate));
	}

	public void testPostStart() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		this.testedInstance = EJBTestUtils.newInstance(TaskSchedulerBean.class,
				container, entityManager);

		Task testTaskComponent = new TestTask();
		String testTaskJndiName = "SafeOnline/task/TestTaskComponent";
		this.jndiTestUtils.bindComponent(testTaskJndiName, testTaskComponent);

		// operate
		LOG
				.debug("------------------ FIRST POST START -------------------------");
		this.testedInstance.postStart();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();

		SchedulingDAO schedulingDAO = EJBTestUtils.newInstance(
				SchedulingDAOBean.class, container, entityManager);
		SchedulingEntity defaultScheduling = schedulingDAO
				.findSchedulingByName("default");
		assertNotNull(defaultScheduling);
		List<TaskEntity> defaultTasks = defaultScheduling.getTasks();
		assertNotNull(defaultTasks);
		assertEquals(1, defaultTasks.size());
		assertEquals(testTaskJndiName, defaultTasks.get(0).getJndiName());
		assertEquals("test-task", defaultTasks.get(0).getName());
		assertEquals(defaultScheduling, defaultTasks.get(0).getScheduling());

		LOG
				.debug("------------------ SECOND POST START ----------------------------");
		/*
		 * We run postStart twice since the task scheduler bean must be capable
		 * of rebooting using a non-volatile database.
		 */
		this.testedInstance.postStart();
	}

	private static class TestTask implements Task {

		private static final Log LOG = LogFactory.getLog(TestTask.class);

		public String getName() {
			return "test-task";
		}

		public void perform() {
			LOG.debug("perform");
		}
	}
}
