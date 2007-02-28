/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import java.util.Date;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.model.bean.TaskSchedulerBean;
import net.link.safeonline.test.util.EJBTestUtils;
import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;

public class TaskSchedulerBeanTest extends TestCase {

	private TaskSchedulerBean testedInstance;
	
	private TimerService mockTimerService;
	private Timer mockTimer;
	
	@Override
	public void setUp() throws Exception {
		this.mockTimerService = createMock(TimerService.class);
		this.mockTimer = createMock(Timer.class);
		this.testedInstance = new TaskSchedulerBean();
		EJBTestUtils.inject(this.testedInstance, this.mockTimerService);
		
	}
	
	public void testSetTimer() {
		// setup
		SchedulingEntity scheduling = new SchedulingEntity("test","0 0/5 * * * ?",null);
		
		expect(this.mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(this.mockTimer);
		expect(this.mockTimer.getHandle()).andReturn(null);
		expect(this.mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(this.mockTimer);
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
	
}
