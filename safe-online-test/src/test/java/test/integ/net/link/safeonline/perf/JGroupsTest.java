package test.integ.net.link.safeonline.perf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;

import org.junit.Test;

/**
 * 
 * @author mbillemo
 */
public class JGroupsTest {

	@Test
	public void testDiscoverer() throws Exception {

		AgentDiscoverer discoverer = new AgentDiscoverer();

		// Check validity of JGroups Channel.
		assertTrue(discoverer.isConnected());
		assertFalse(discoverer.getGroupName() == null
				|| discoverer.getGroupName().length() == 0);
		assertFalse(discoverer.getSelf() == null);
		assertFalse(discoverer.getMembers().isEmpty());

		discoverer.close();
	}
}
